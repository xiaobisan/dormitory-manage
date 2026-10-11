package cn.dormitory.eval.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.entity.SysUser;
import cn.dormitory.common.entity.Violation;
import cn.dormitory.common.entity.ViolationType;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.eval.dto.DeptViolationStatsVO;
import cn.dormitory.eval.dto.ViolationHandleDTO;
import cn.dormitory.eval.dto.ViolationRecordDTO;
import cn.dormitory.eval.dto.ViolationRecordVO;
import cn.dormitory.eval.mapper.RoomEvalQueryMapper;
import cn.dormitory.eval.mapper.SysUserQueryMapper;

import java.util.LinkedHashSet;
import java.util.Set;
import cn.dormitory.eval.mapper.ViolationMapper;
import cn.dormitory.eval.mapper.ViolationTypeEvalMapper;
import cn.dormitory.eval.service.ViolationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ViolationServiceImpl implements ViolationService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ViolationMapper violationMapper;
    private final ViolationTypeEvalMapper typeMapper;
    private final RoomEvalQueryMapper roomMapper;
    private final SysUserQueryMapper userMapper;

    // ==================== 录入 ====================

    @Override
    @Transactional
    public Violation createRecord(ViolationRecordDTO dto) {
        if (dto == null || dto.getStudentId() == null) {
            throw new BusinessException("缺少 studentId");
        }
        if (dto.getRoomId() == null) {
            throw new BusinessException("缺少 roomId");
        }
        if (dto.getTypeId() == null) {
            throw new BusinessException("缺少 typeId");
        }
        // 校验学生存在
        SysUser student = userMapper.selectById(dto.getStudentId());
        if (student == null) {
            throw BusinessException.notFound("学生不存在: " + dto.getStudentId());
        }
        // 校验宿舍存在
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw BusinessException.notFound("宿舍不存在: " + dto.getRoomId());
        }
        // 校验违纪类型存在
        if (typeMapper.selectById(dto.getTypeId()) == null) {
            throw BusinessException.notFound("违纪类型不存在: " + dto.getTypeId());
        }
        // 辅导员数据隔离：只能录本系学生
        if (UserContext.hasRole("COUNSELOR")) {
            UserContext ctx = UserContext.current();
            if (ctx.getDataScopeId() == null || !ctx.getDataScopeId().equals(student.getDeptId())) {
                throw BusinessException.forbidden("辅导员只能录入本系学生的违纪");
            }
        }
        // 宿管数据隔离：只能录本楼栋的宿舍
        if (UserContext.hasRole("DORM_MANAGER")) {
            UserContext ctx = UserContext.current();
            if (ctx.getDataScopeId() == null || !ctx.getDataScopeId().equals(room.getBuildingId())) {
                throw BusinessException.forbidden("宿管只能录入本楼栋的违纪");
            }
        }

        Violation v = new Violation();
        v.setTypeId(dto.getTypeId());
        v.setStudentId(dto.getStudentId());
        v.setRoomId(dto.getRoomId());
        v.setHappenedAt(dto.getHappenedAt() != null ? dto.getHappenedAt() : LocalDateTime.now());
        v.setSeverity(dto.getSeverity());
        v.setDescription(dto.getDescription());
        v.setRemark(dto.getDescription());
        violationMapper.insert(v);
        return v;
    }

    // ==================== 列表 ====================

    @Override
    public PageResult<ViolationRecordVO> page(long current, long size,
                                               Long studentId, Long roomId, Long deptId,
                                               String fromMonth, String toMonth) {
        UserContext ctx = UserContext.current();
        boolean manager = UserContext.hasRole("DORM_MANAGER");

        // 辅导员默认只查本系
        if (UserContext.hasRole("COUNSELOR") && deptId == null) {
            deptId = ctx != null ? ctx.getDataScopeId() : null;
            if (deptId == null) {
                throw BusinessException.forbidden("辅导员数据范围未配置");
            }
        }
        // 系别过滤：取本系学生 id 集合（空集合=无学生，直接返回空页）
        Set<Long> studentIds = null;
        if (deptId != null) {
            studentIds = studentIdsOfDept(deptId);
            if (studentIds.isEmpty()) {
                return PageResult.empty(current, size);
            }
        }

        LambdaQueryWrapper<Violation> w = new LambdaQueryWrapper<>();
        w.eq(studentId != null, Violation::getStudentId, studentId);
        // 宿舍过滤：指定了宿舍则等值匹配，并做角色校验
        if (roomId != null) {
            Room room = roomMapper.selectById(roomId);
            if (room == null) {
                throw BusinessException.notFound("宿舍不存在: " + roomId);
            }
            if (manager && (ctx == null || ctx.getDataScopeId() == null
                    || !ctx.getDataScopeId().equals(room.getBuildingId()))) {
                throw BusinessException.forbidden("宿管只能查询本楼栋的违纪");
            }
            w.eq(Violation::getRoomId, roomId);
        } else if (manager) {
            // 宿管未指定宿舍：限定本楼栋宿舍集合
            Long buildingId = ctx != null ? ctx.getDataScopeId() : null;
            if (buildingId == null) {
                throw BusinessException.forbidden("宿管数据范围未配置");
            }
            List<Long> roomIds = roomIdsOfBuilding(buildingId);
            if (roomIds.isEmpty()) {
                return PageResult.empty(current, size);
            }
            w.in(Violation::getRoomId, roomIds);
        }
        // 系别（学生）过滤
        if (studentIds != null) {
            w.in(Violation::getStudentId, studentIds);
        }
        // 时间区间（按 happenedAt）
        if (fromMonth != null && !fromMonth.isBlank()) {
            w.ge(Violation::getHappenedAt, parseMonthStart(fromMonth));
        }
        if (toMonth != null && !toMonth.isBlank()) {
            w.lt(Violation::getHappenedAt, parseMonthStart(toMonth).plusMonths(1));
        }
        w.orderByDesc(Violation::getHappenedAt);

        IPage<Violation> p = violationMapper.selectPage(new Page<>(current, size), w);
        return toVoPage(p);
    }

    private LocalDateTime parseMonthStart(String month) {
        return YearMonth.parse(month, MONTH_FMT).atDay(1).atStartOfDay();
    }

    /** 转 VO 并批量补全学生/宿舍/类型信息。 */
    private PageResult<ViolationRecordVO> toVoPage(IPage<Violation> p) {
        List<Violation> records = p.getRecords();
        List<ViolationRecordVO> vos = new ArrayList<>();
        for (Violation v : records) {
            vos.add(ViolationRecordVO.of(v));
        }
        fillVos(vos);
        PageResult<ViolationRecordVO> pr = new PageResult<>();
        pr.setCurrent(p.getCurrent());
        pr.setSize(p.getSize());
        pr.setTotal(p.getTotal());
        pr.setRecords(vos);
        return pr;
    }

    /** 批量补全：学生姓名、宿舍号/楼栋、类型名/码。 */
    private void fillVos(List<ViolationRecordVO> vos) {
        if (vos.isEmpty()) {
            return;
        }
        Set<Long> studentIdSet = new HashSet<>();
        Set<Long> roomIdSet = new HashSet<>();
        Set<Long> typeIdSet = new HashSet<>();
        for (ViolationRecordVO vo : vos) {
            if (vo.getStudentId() != null) studentIdSet.add(vo.getStudentId());
            if (vo.getRoomId() != null) roomIdSet.add(vo.getRoomId());
            if (vo.getTypeId() != null) typeIdSet.add(vo.getTypeId());
        }
        Map<Long, SysUser> studentMap = loadUserByIds(studentIdSet);
        Map<Long, Room> roomMap = loadRoomByIds(roomIdSet);
        Map<Long, ViolationType> typeMap = loadTypeByIds(typeIdSet);
        for (ViolationRecordVO vo : vos) {
            SysUser s = studentMap.get(vo.getStudentId());
            vo.fillStudent(s);
            Room r = roomMap.get(vo.getRoomId());
            if (r != null) {
                vo.fillRoom(r.getId(), r.getBuildingId(), r.getRoomNo());
            }
            ViolationType t = typeMap.get(vo.getTypeId());
            if (t != null) {
                vo.fillType(t.getCode(), t.getName());
            }
        }
    }

    /** 按 id 集合批量加载学生为 Map。 */
    private Map<Long, SysUser> loadUserByIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<SysUser> list = userMapper.selectBatchIds(ids);
        Map<Long, SysUser> m = new HashMap<>();
        for (SysUser u : list) {
            m.put(u.getId(), u);
        }
        return m;
    }

    private Map<Long, Room> loadRoomByIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Room> list = roomMapper.selectBatchIds(ids);
        Map<Long, Room> m = new HashMap<>();
        for (Room r : list) {
            m.put(r.getId(), r);
        }
        return m;
    }

    private Map<Long, ViolationType> loadTypeByIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<ViolationType> list = typeMapper.selectBatchIds(ids);
        Map<Long, ViolationType> m = new HashMap<>();
        for (ViolationType t : list) {
            m.put(t.getId(), t);
        }
        return m;
    }

    /** 本系学生 id 集合（role=STUDENT 且 deptId 匹配）。 */
    private Set<Long> studentIdsOfDept(Long deptId) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "STUDENT")
                .eq(SysUser::getDeptId, deptId)
                .select(SysUser::getId);
        List<SysUser> list = userMapper.selectList(w);
        Set<Long> ids = new LinkedHashSet<>();
        for (SysUser u : list) {
            ids.add(u.getId());
        }
        return ids;
    }

    /** 楼栋下宿舍 id 集合。 */
    private List<Long> roomIdsOfBuilding(Long buildingId) {
        LambdaQueryWrapper<Room> w = new LambdaQueryWrapper<Room>()
                .eq(Room::getBuildingId, buildingId)
                .select(Room::getId);
        List<Room> rooms = roomMapper.selectList(w);
        List<Long> ids = new ArrayList<>();
        for (Room r : rooms) {
            ids.add(r.getId());
        }
        return ids;
    }

    // ==================== 处理 ====================

    @Override
    @Transactional
    public void handle(Long id, ViolationHandleDTO dto) {
        if (id == null) {
            throw new BusinessException("缺少违纪记录 ID");
        }
        Violation v = violationMapper.selectById(id);
        if (v == null) {
            throw BusinessException.notFound("违纪记录不存在: " + id);
        }
        LocalDateTime handledAt = dto != null && dto.getHandledAt() != null
                ? dto.getHandledAt()
                : LocalDateTime.now();
        v.setHandledAt(handledAt);
        if (dto != null && dto.getHandleNote() != null) {
            v.setHandleNote(dto.getHandleNote());
        }
        violationMapper.updateById(v);
    }

    // ==================== 详情 ====================

    @Override
    public ViolationRecordVO detail(Long id) {
        Violation v = violationMapper.selectById(id);
        if (v == null) {
            throw BusinessException.notFound("违纪记录不存在: " + id);
        }
        ViolationRecordVO vo = ViolationRecordVO.of(v);
        fillVos(List.of(vo));
        return vo;
    }

    // ==================== 系别统计 ====================

    @Override
    public DeptViolationStatsVO statsByDept(Long deptId, String fromMonth, String toMonth) {
        // 辅导员：deptId 缺省读自己的数据范围
        if (deptId == null) {
            UserContext ctx = UserContext.current();
            if (ctx != null && ctx.getDataScopeId() != null) {
                deptId = ctx.getDataScopeId();
            } else if (UserContext.hasRole("COUNSELOR")) {
                throw BusinessException.forbidden("辅导员数据范围未配置");
            } else {
                // 管理员未传 deptId 则统计全部
                deptId = null;
            }
        }

        Set<Long> studentIds = null;
        if (deptId != null) {
            studentIds = studentIdsOfDept(deptId);
        }

        // 默认区间：近 12 个月
        YearMonth now = YearMonth.now();
        YearMonth to = toMonth != null && !toMonth.isBlank()
                ? YearMonth.parse(toMonth, MONTH_FMT) : now;
        YearMonth from = fromMonth != null && !fromMonth.isBlank()
                ? YearMonth.parse(fromMonth, MONTH_FMT) : to.minusMonths(11);

        // 拉取区间内全部违纪（Java 聚合）
        LambdaQueryWrapper<Violation> w = new LambdaQueryWrapper<Violation>()
                .ge(Violation::getHappenedAt, from.atDay(1).atStartOfDay())
                .lt(Violation::getHappenedAt, to.plusMonths(1).atDay(1).atStartOfDay());
        if (studentIds != null) {
            if (studentIds.isEmpty()) {
                return emptyStats(deptId, from, to);
            }
            w.in(Violation::getStudentId, studentIds);
        }
        w.orderByAsc(Violation::getHappenedAt);
        List<Violation> list = violationMapper.selectList(w);

        // 聚合
        Map<Long, Integer> byTypeMap = new LinkedHashMap<>();
        Map<String, Integer> byMonthMap = new LinkedHashMap<>();
        Set<Long> studentSet = new HashSet<>();
        for (Violation v : list) {
            if (v.getTypeId() != null) {
                byTypeMap.merge(v.getTypeId(), 1, Integer::sum);
            }
            if (v.getHappenedAt() != null) {
                String m = v.getHappenedAt().toLocalDate().format(MONTH_FMT);
                byMonthMap.merge(m, 1, Integer::sum);
            }
            if (v.getStudentId() != null) {
                studentSet.add(v.getStudentId());
            }
        }

        // 月份趋势补 0（from..to）
        List<DeptViolationStatsVO.MonthCount> monthTrend = new ArrayList<>();
        for (YearMonth m = from; m.compareTo(to) <= 0; m = m.plusMonths(1)) {
            String key = m.format(MONTH_FMT);
            monthTrend.add(new DeptViolationStatsVO.MonthCount(key,
                    byMonthMap.getOrDefault(key, 0)));
        }

        // 类型计数补全
        Map<Long, ViolationType> typeMap = loadTypeByIds(new HashSet<>(byTypeMap.keySet()));
        List<DeptViolationStatsVO.TypeCount> typeCounts = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : byTypeMap.entrySet()) {
            ViolationType t = typeMap.get(e.getKey());
            typeCounts.add(new DeptViolationStatsVO.TypeCount(
                    e.getKey(),
                    t != null ? t.getCode() : null,
                    t != null ? t.getName() : "未知类型",
                    e.getValue()));
        }

        DeptViolationStatsVO vo = new DeptViolationStatsVO();
        vo.setDeptId(deptId);
        vo.setFromMonth(from.format(MONTH_FMT));
        vo.setToMonth(to.format(MONTH_FMT));
        vo.setTotalRecords(list.size());
        vo.setTotalStudents(studentSet.size());
        vo.setByType(typeCounts);
        vo.setByMonth(monthTrend);
        return vo;
    }

    private DeptViolationStatsVO emptyStats(Long deptId, YearMonth from, YearMonth to) {
        DeptViolationStatsVO vo = new DeptViolationStatsVO();
        vo.setDeptId(deptId);
        vo.setFromMonth(from.format(MONTH_FMT));
        vo.setToMonth(to.format(MONTH_FMT));
        vo.setTotalRecords(0);
        vo.setTotalStudents(0);
        List<DeptViolationStatsVO.MonthCount> months = new ArrayList<>();
        for (YearMonth m = from; m.compareTo(to) <= 0; m = m.plusMonths(1)) {
            months.add(new DeptViolationStatsVO.MonthCount(m.format(MONTH_FMT), 0));
        }
        vo.setByMonth(months);
        vo.setByType(List.of());
        return vo;
    }
}
