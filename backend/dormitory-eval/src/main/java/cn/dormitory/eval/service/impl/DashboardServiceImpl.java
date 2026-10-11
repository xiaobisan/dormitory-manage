package cn.dormitory.eval.service.impl;

import cn.dormitory.common.entity.Bed;
import cn.dormitory.common.entity.Bill;
import cn.dormitory.common.entity.Building;
import cn.dormitory.common.entity.Enrollment;
import cn.dormitory.common.entity.EvaluationScore;
import cn.dormitory.common.entity.RepairOrder;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.entity.SysUser;
import cn.dormitory.common.entity.Violation;
import cn.dormitory.common.entity.ViolationType;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.eval.dto.DashboardVO;
import cn.dormitory.eval.mapper.BedQueryMapper;
import cn.dormitory.eval.mapper.BillQueryMapper;
import cn.dormitory.eval.mapper.BuildingQueryMapper;
import cn.dormitory.eval.mapper.EnrollmentQueryMapper;
import cn.dormitory.eval.mapper.EvaluationScoreQueryMapper;
import cn.dormitory.eval.mapper.RepairOrderQueryMapper;
import cn.dormitory.eval.mapper.RoomEvalQueryMapper;
import cn.dormitory.eval.mapper.SysUserQueryMapper;
import cn.dormitory.eval.mapper.ViolationMapper;
import cn.dormitory.eval.service.DashboardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int TOP_N = 5;

    private final EnrollmentQueryMapper enrollmentMapper;
    private final BedQueryMapper bedMapper;
    private final RoomEvalQueryMapper roomMapper;
    private final BillQueryMapper billMapper;
    private final RepairOrderQueryMapper repairMapper;
    private final ViolationMapper violationMapper;
    private final SysUserQueryMapper userMapper;
    private final EvaluationScoreQueryMapper evalScoreMapper;
    private final BuildingQueryMapper buildingMapper;

    @Override
    public DashboardVO overview() {
        // ===== 解析数据范围 =====
        UserContext ctx = UserContext.current();
        String role = ctx != null ? ctx.getRole() : null;
        Long scopeId = ctx != null ? ctx.getDataScopeId() : null;

        Set<Long> buildingIds = null;   // 楼栋维度（宿管）
        Set<Long> studentIds = null;    // 系别维度（辅导员）

        if ("DORM_MANAGER".equals(role)) {
            buildingIds = setOf(scopeId);
        } else if ("COUNSELOR".equals(role)) {
            studentIds = studentIdsOfDept(scopeId);
        }
        // ADMIN / REPAIR_WORKER：全量（buildingIds、studentIds 均 null）

        String scopeLabel = buildScopeLabel(role, buildingIds);

        // 范围限定参数：楼栋 ID（按 room.building_id）或学生 ID（按 enrollment.student_id）
        // 注意：宿舍/床位/账单/工单按楼栋过滤；违纪按学生（系别）过滤。
        DashboardVO vo = new DashboardVO();
        vo.setScopeLabel(scopeLabel);

        // ===== 入住率 =====
        fillOccupancy(vo, buildingIds, studentIds);

        // ===== 水电费收缴率 =====
        fillBillCollection(vo, buildingIds, studentIds);

        // ===== 报修时效 =====
        fillRepairTimeliness(vo, buildingIds, studentIds);

        // ===== 违纪趋势（近 6 个月） =====
        fillViolationTrend(vo, buildingIds, studentIds);

        // ===== 卫生分 Top 宿舍 =====
        fillTopRooms(vo, buildingIds, studentIds);

        return vo;
    }

    private Set<Long> setOf(Long id) {
        java.util.Set<Long> s = new java.util.HashSet<>();
        if (id != null) {
            s.add(id);
        }
        return s;
    }

    private String buildScopeLabel(String role, Set<Long> buildingIds) {
        if ("ADMIN".equals(role) || "REPAIR_WORKER".equals(role)) {
            return "全部";
        }
        if ("DORM_MANAGER".equals(role)) {
            if (buildingIds != null && !buildingIds.isEmpty()) {
                Long bid = buildingIds.iterator().next();
                Building b = buildingMapper.selectById(bid);
                return b != null ? b.getName() : ("楼栋 " + bid);
            }
            return "全部";
        }
        if ("COUNSELOR".equals(role)) {
            UserContext ctx = UserContext.current();
            Long deptId = ctx != null ? ctx.getDataScopeId() : null;
            return deptId != null ? ("系别 " + deptId) : "全部";
        }
        return "全部";
    }

    // ============ 各指标填充（均做范围过滤） ============

    private void fillOccupancy(DashboardVO vo, Set<Long> buildingIds, Set<Long> studentIds) {
        // 床位总数：按楼栋过滤（宿管）；辅导员/管理员全量床位
        LambdaQueryWrapper<Bed> bedW = new LambdaQueryWrapper<>();
        if (buildingIds != null && !buildingIds.isEmpty()) {
            bedW.in(Bed::getRoomId, roomIdsOfBuildings(buildingIds));
        }
        int totalBeds = Math.toIntExact(bedMapper.selectCount(bedW));

        // ACTIVE 入住数：按学生（辅导员）或楼栋（宿管）过滤
        LambdaQueryWrapper<Enrollment> enW = new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getStatus, "ACTIVE");
        if (buildingIds != null && !buildingIds.isEmpty()) {
            enW.in(Enrollment::getBuildingId, buildingIds);
        } else if (studentIds != null) {
            if (studentIds.isEmpty()) {
                vo.setActiveEnrollmentCount(0);
                vo.setTotalBedCount(totalBeds);
                vo.setOccupancyRate(rate(0, totalBeds));
                return;
            }
            enW.in(Enrollment::getStudentId, studentIds);
        }
        int active = Math.toIntExact(enrollmentMapper.selectCount(enW));

        vo.setActiveEnrollmentCount(active);
        vo.setTotalBedCount(totalBeds);
        vo.setOccupancyRate(rate(active, totalBeds));
    }

    private void fillBillCollection(DashboardVO vo, Set<Long> buildingIds, Set<Long> studentIds) {
        // 账单按楼栋过滤（辅导员/管理员不限楼栋）；辅导员按学生所在宿舍范围——
        // 账单无学生字段，辅导员范围用"本系学生当前入住宿舍"近似。
        LambdaQueryWrapper<Bill> w = new LambdaQueryWrapper<>();
        List<Long> roomIds = null;
        if (buildingIds != null && !buildingIds.isEmpty()) {
            roomIds = roomIdsOfBuildings(buildingIds);
        } else if (studentIds != null) {
            if (studentIds.isEmpty()) {
                vo.setPaidBillCount(0);
                vo.setTotalBillCount(0);
                vo.setBillCollectionRate(rate(0, 0));
                return;
            }
            roomIds = roomIdsOfStudents(studentIds);
        }
        if (roomIds != null) {
            if (roomIds.isEmpty()) {
                vo.setPaidBillCount(0);
                vo.setTotalBillCount(0);
                vo.setBillCollectionRate(rate(0, 0));
                return;
            }
            w.in(Bill::getRoomId, roomIds);
        }
        int total = Math.toIntExact(billMapper.selectCount(w));
        // 已缴：复制楼栋/宿舍范围条件 + PAID
        LambdaQueryWrapper<Bill> paid = new LambdaQueryWrapper<>();
        if (roomIds != null) {
            paid.in(Bill::getRoomId, roomIds);
        }
        paid.eq(Bill::getStatus, "PAID");
        int paidCount = Math.toIntExact(billMapper.selectCount(paid));

        vo.setPaidBillCount(paidCount);
        vo.setTotalBillCount(total);
        vo.setBillCollectionRate(rate(paidCount, total));
    }

    private void fillRepairTimeliness(DashboardVO vo, Set<Long> buildingIds, Set<Long> studentIds) {
        LambdaQueryWrapper<RepairOrder> w = new LambdaQueryWrapper<>();
        List<Long> roomIds = null;
        if (buildingIds != null && !buildingIds.isEmpty()) {
            roomIds = roomIdsOfBuildings(buildingIds);
        } else if (studentIds != null) {
            if (studentIds.isEmpty()) {
                vo.setRepairStatusCounts(List.of());
                vo.setAvgRepairHours(null);
                return;
            }
            roomIds = roomIdsOfStudents(studentIds);
        }
        if (roomIds != null && !roomIds.isEmpty()) {
            w.in(RepairOrder::getRoomId, roomIds);
        }
        List<RepairOrder> orders = repairMapper.selectList(w);

        // 各状态计数
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        for (String s : new String[]{"PENDING", "ASSIGNED", "PROCESSING", "DONE", "REJECTED"}) {
            statusMap.put(s, 0);
        }
        for (RepairOrder o : orders) {
            if (o.getStatus() != null) {
                statusMap.merge(o.getStatus(), 1, Integer::sum);
            }
        }
        List<DashboardVO.StatusCount> counts = new ArrayList<>();
        for (Map.Entry<String, Integer> e : statusMap.entrySet()) {
            counts.add(new DashboardVO.StatusCount(e.getKey(), e.getValue()));
        }
        vo.setRepairStatusCounts(counts);

        // 平均处理时长：DONE 工单 resolvedAt - createdAt（小时）
        double totalHours = 0;
        int done = 0;
        for (RepairOrder o : orders) {
            if ("DONE".equals(o.getStatus()) && o.getResolvedAt() != null && o.getCreatedAt() != null) {
                double h = Duration.between(o.getCreatedAt(), o.getResolvedAt()).toMinutes() / 60.0;
                totalHours += h;
                done++;
            }
        }
        vo.setAvgRepairHours(done == 0 ? null
                : BigDecimal.valueOf(totalHours / done).setScale(1, RoundingMode.HALF_UP));
    }

    private void fillViolationTrend(DashboardVO vo, Set<Long> buildingIds, Set<Long> studentIds) {
        YearMonth now = YearMonth.now();
        YearMonth from = now.minusMonths(5); // 含本月亮共 6 个月

        LambdaQueryWrapper<Violation> w = new LambdaQueryWrapper<Violation>()
                .ge(Violation::getHappenedAt, from.atDay(1).atStartOfDay())
                .lt(Violation::getHappenedAt, now.plusMonths(1).atDay(1).atStartOfDay());
        if (studentIds != null) {
            if (studentIds.isEmpty()) {
                vo.setViolationTrend(zeroTrend(from, now));
                return;
            }
            w.in(Violation::getStudentId, studentIds);
        } else if (buildingIds != null && !buildingIds.isEmpty()) {
            // 宿管：按宿舍所属楼栋过滤（违纪记录关联宿舍）
            List<Long> roomIds = roomIdsOfBuildings(buildingIds);
            if (roomIds.isEmpty()) {
                vo.setViolationTrend(zeroTrend(from, now));
                return;
            }
            w.in(Violation::getRoomId, roomIds);
        }
        List<Violation> list = violationMapper.selectList(w);

        Map<String, Integer> byMonth = new LinkedHashMap<>();
        for (Violation v : list) {
            if (v.getHappenedAt() != null) {
                byMonth.merge(v.getHappenedAt().toLocalDate().format(MONTH_FMT), 1, Integer::sum);
            }
        }
        List<cn.dormitory.eval.dto.DeptViolationStatsVO.MonthCount> trend = new ArrayList<>();
        for (YearMonth m = from; m.compareTo(now) <= 0; m = m.plusMonths(1)) {
            String key = m.format(MONTH_FMT);
            trend.add(new cn.dormitory.eval.dto.DeptViolationStatsVO.MonthCount(
                    key, byMonth.getOrDefault(key, 0)));
        }
        vo.setViolationTrend(trend);
    }

    private List<cn.dormitory.eval.dto.DeptViolationStatsVO.MonthCount> zeroTrend(YearMonth from, YearMonth to) {
        List<cn.dormitory.eval.dto.DeptViolationStatsVO.MonthCount> t = new ArrayList<>();
        for (YearMonth m = from; m.compareTo(to) <= 0; m = m.plusMonths(1)) {
            t.add(new cn.dormitory.eval.dto.DeptViolationStatsVO.MonthCount(m.format(MONTH_FMT), 0));
        }
        return t;
    }

    private void fillTopRooms(DashboardVO vo, Set<Long> buildingIds, Set<Long> studentIds) {
        // 最近周期 = evaluation_score 中最大 period（Java 侧取）
        List<EvaluationScore> all = evalScoreMapper.selectList(new LambdaQueryWrapper<EvaluationScore>());
        if (all.isEmpty()) {
            vo.setTopRooms(List.of());
            return;
        }
        String latestPeriod = all.stream()
                .map(EvaluationScore::getPeriod)
                .filter(p -> p != null && !p.isBlank())
                .max(String::compareTo)
                .orElse(null);
        if (latestPeriod == null) {
            vo.setTopRooms(List.of());
            return;
        }
        LambdaQueryWrapper<EvaluationScore> w = new LambdaQueryWrapper<EvaluationScore>()
                .eq(EvaluationScore::getPeriod, latestPeriod)
                .isNotNull(EvaluationScore::getTotalScore)
                .orderByDesc(EvaluationScore::getTotalScore)
                .last("LIMIT " + TOP_N);
        List<EvaluationScore> top = evalScoreMapper.selectList(w);

        // 补宿舍号/楼栋
        Map<Long, Room> roomMap = loadRooms(top.stream().map(EvaluationScore::getRoomId).filter(java.util.Objects::nonNull).toList());
        Map<Long, Building> buildingMap = loadBuildings(roomMap.values().stream().map(Room::getBuildingId).filter(java.util.Objects::nonNull).distinct().toList());

        List<DashboardVO.TopRoomScore> result = new ArrayList<>();
        for (EvaluationScore s : top) {
            DashboardVO.TopRoomScore t = new DashboardVO.TopRoomScore();
            t.setRoomId(s.getRoomId());
            t.setPeriod(s.getPeriod());
            t.setTotalScore(s.getTotalScore());
            t.setStarLevel(s.getStarLevel());
            Room r = roomMap.get(s.getRoomId());
            if (r != null) {
                t.setRoomNo(r.getRoomNo());
                t.setBuildingId(r.getBuildingId());
                Building b = buildingMap.get(r.getBuildingId());
                if (b != null) {
                    t.setBuildingName(b.getName());
                }
            }
            result.add(t);
        }
        vo.setTopRooms(result);
    }

    // ============ 范围辅助 ============

    private List<Long> roomIdsOfBuildings(Set<Long> buildingIds) {
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .in(Room::getBuildingId, buildingIds)
                .select(Room::getId));
        List<Long> ids = new ArrayList<>();
        for (Room r : rooms) {
            ids.add(r.getId());
        }
        return ids;
    }

    /** 本系学生当前入住（ACTIVE）的宿舍集合。 */
    private List<Long> roomIdsOfStudents(Set<Long> studentIds) {
        List<Enrollment> en = enrollmentMapper.selectList(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getStatus, "ACTIVE")
                .in(Enrollment::getStudentId, studentIds)
                .select(Enrollment::getRoomId));
        List<Long> ids = new ArrayList<>();
        for (Enrollment e : en) {
            if (e.getRoomId() != null) {
                ids.add(e.getRoomId());
            }
        }
        return ids;
    }

    /** 系别下学生 ID 集合（role=STUDENT 且 deptId 匹配）。 */
    private Set<Long> studentIdsOfDept(Long deptId) {
        java.util.Set<Long> ids = new java.util.LinkedHashSet<>();
        if (deptId == null) {
            return ids;
        }
        List<SysUser> list = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "STUDENT")
                .eq(SysUser::getDeptId, deptId)
                .select(SysUser::getId));
        for (SysUser u : list) {
            ids.add(u.getId());
        }
        return ids;
    }

    private Map<Long, Room> loadRooms(List<Long> ids) {
        Map<Long, Room> m = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return m;
        }
        for (Room r : roomMapper.selectBatchIds(ids)) {
            m.put(r.getId(), r);
        }
        return m;
    }

    private Map<Long, Building> loadBuildings(List<Long> ids) {
        Map<Long, Building> m = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return m;
        }
        for (Building b : buildingMapper.selectBatchIds(ids)) {
            m.put(b.getId(), b);
        }
        return m;
    }

    /** 比率（0~1，两位小数；分母 0 返回 0）。 */
    private BigDecimal rate(int num, int den) {
        if (den == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(num).divide(BigDecimal.valueOf(den), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
