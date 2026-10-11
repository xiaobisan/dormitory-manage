package cn.dormitory.maintenance.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.CheckRecord;
import cn.dormitory.common.entity.EvaluationAward;
import cn.dormitory.common.entity.EvaluationRule;
import cn.dormitory.common.entity.EvaluationScore;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.maintenance.dto.EvaluationScoreDetailDTO;
import cn.dormitory.maintenance.mapper.EvaluationAwardMapper;
import cn.dormitory.maintenance.mapper.EvaluationScoreMapper;
import cn.dormitory.maintenance.mapper.RoomQueryMapper;
import cn.dormitory.maintenance.mapper.ViolationQueryMapper;
import cn.dormitory.maintenance.mapper.ViolationQueryMapper.ViolationDeductionRow;
import cn.dormitory.maintenance.service.CheckRecordService;
import cn.dormitory.maintenance.service.EvaluationRuleService;
import cn.dormitory.maintenance.service.EvaluationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评优量化引擎实现（可追溯）。
 * <pre>
 *   ① 卫生分  hygieneScore      = 该宿舍该周期最近一次 CheckRecord.score（缺 0）
 *   ② 违纪扣分 violationDeduction = sum(violation_type.default_deduction)
 *      违纪分  disciplineScore   = 100 - violationDeduction（下限 0）
 *   ③ 加分项  bonusScore        = 入参（可配，缺 0）
 *   总分 totalScore = 卫生分×卫生权重 + 违纪分×违纪权重 + 加分×加分权重
 *   星级 starLevel / 称号 hitTitle = 按 rule.thresholds（JSON）命中最高档
 * </pre>
 */
@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRuleService ruleService;
    private final CheckRecordService checkRecordService;
    private final ViolationQueryMapper violationQueryMapper;
    private final EvaluationScoreMapper scoreMapper;
    private final EvaluationAwardMapper awardMapper;
    private final RoomQueryMapper roomQueryMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluationServiceImpl(EvaluationRuleService ruleService,
                                 CheckRecordService checkRecordService,
                                 ViolationQueryMapper violationQueryMapper,
                                 EvaluationScoreMapper scoreMapper,
                                 EvaluationAwardMapper awardMapper,
                                 RoomQueryMapper roomQueryMapper) {
        this.ruleService = ruleService;
        this.checkRecordService = checkRecordService;
        this.violationQueryMapper = violationQueryMapper;
        this.scoreMapper = scoreMapper;
        this.awardMapper = awardMapper;
        this.roomQueryMapper = roomQueryMapper;
    }

    @Override
    public EvaluationScoreDetailDTO calculateScore(String period, Long roomId) {
        return calculateScore(period, roomId, null);
    }

    @Override
    @Transactional
    public EvaluationScoreDetailDTO calculateScore(String period, Long roomId, BigDecimal bonusScore) {
        if (period == null || period.isBlank()) throw new BusinessException("缺少周期 period(yyyy-MM)");
        if (roomId == null) throw new BusinessException("缺少 roomId");
        Room room = roomQueryMapper.selectById(roomId);
        if (room == null) throw BusinessException.notFound("宿舍不存在: " + roomId);

        EvaluationRule rule = ruleService.active();
        BigDecimal bonus = (bonusScore == null ? BigDecimal.ZERO : bonusScore).setScale(2, RoundingMode.HALF_UP);

        EvaluationScoreDetailDTO dto = new EvaluationScoreDetailDTO();
        dto.setRoomId(roomId);
        dto.setPeriod(period);
        dto.setRoomNo(room.getRoomNo());
        dto.setRuleId(rule.getId());
        dto.setRuleName(rule.getName());
        dto.setHygieneWeight(rule.getHygieneWeight());
        dto.setDisciplineWeight(rule.getDisciplineWeight());
        dto.setBonusWeight(rule.getBonusWeight());

        // ① 卫生分：该宿舍该周期最近一次检查得分
        CheckRecord latest = checkRecordService.latestRecordOfRoomPeriod(roomId, period);
        BigDecimal hygiene = (latest == null || latest.getScore() == null) ? BigDecimal.ZERO : latest.getScore();
        hygiene = hygiene.setScale(2, RoundingMode.HALF_UP);
        dto.setHygieneScore(hygiene);

        // ② 违纪扣分 + 明细
        BigDecimal deduction = violationQueryMapper.sumDeductionByRoomAndPeriod(roomId, period);
        if (deduction == null) deduction = BigDecimal.ZERO;
        deduction = deduction.setScale(2, RoundingMode.HALF_UP);
        dto.setViolationDeduction(deduction);
        List<ViolationDeductionRow> rows = violationQueryMapper.deductionDetailByRoomAndPeriod(roomId, period);
        List<EvaluationScoreDetailDTO.ViolationDeductionRow> details = new ArrayList<>();
        for (ViolationDeductionRow r : rows) {
            EvaluationScoreDetailDTO.ViolationDeductionRow d = new EvaluationScoreDetailDTO.ViolationDeductionRow();
            d.setViolationId(r.getViolationId());
            d.setTypeName(r.getTypeName());
            d.setDescription(r.getDescription());
            d.setDeduction(r.getDeduction());
            details.add(d);
        }
        dto.setViolationDetails(details);
        BigDecimal discipline = BigDecimal.valueOf(100).subtract(deduction);
        if (discipline.signum() < 0) discipline = BigDecimal.ZERO;
        discipline = discipline.setScale(2, RoundingMode.HALF_UP);
        dto.setDisciplineScore(discipline);

        // ③ 加分项
        dto.setBonusScore(bonus);

        // 总分 = 卫生×卫生权重 + 违纪分×违纪权重 + 加分×加分权重
        BigDecimal total = hygiene.multiply(scale(rule.getHygieneWeight(), 2))
                .add(discipline.multiply(scale(rule.getDisciplineWeight(), 2)))
                .add(bonus.multiply(scale(rule.getBonusWeight(), 2)));
        total = total.setScale(2, RoundingMode.HALF_UP);
        dto.setTotalScore(total);

        // 阈值 → 星级/称号
        Map<String, BigDecimal> thresholds = parseThresholds(rule);
        int star = resolveStar(total, thresholds);
        String title = resolveTitle(total, thresholds);
        dto.setStarLevel(star);
        dto.setHitTitle(title);

        dto.setCalculationTrace(
                String.format("卫生分 %s×权重 %s = %.2f；违纪分 %s（100-扣%s）×权重 %s = %.2f；加分 %s×权重 %s = %.2f；总分 %.2f → %s 星",
                        fmt(hygiene), fmt(rule.getHygieneWeight()),
                        hygiene.multiply(rule.getHygieneWeight()).doubleValue(),
                        fmt(discipline), fmt(deduction), fmt(rule.getDisciplineWeight()),
                        discipline.multiply(rule.getDisciplineWeight()).doubleValue(),
                        fmt(bonus), fmt(rule.getBonusWeight()),
                        bonus.multiply(rule.getBonusWeight()).doubleValue(),
                        total.doubleValue(), star));

        // upsert evaluation_score（按 roomId+period 唯一）
        EvaluationScore saved = upsertScore(roomId, period, hygiene, discipline, bonus, total, star, rule);
        dto.setSavedScore(saved);
        return dto;
    }

    @Override
    public PageResult<EvaluationScore> pageScores(Long current, Long size, String period, Long buildingId, Long roomId) {
        LambdaQueryWrapper<EvaluationScore> w = new LambdaQueryWrapper<>();
        w.eq(period != null && !period.isBlank(), EvaluationScore::getPeriod, period);
        if (roomId != null) w.eq(EvaluationScore::getRoomId, roomId);
        if (buildingId != null) {
            List<Long> roomIds = roomIdsOfBuilding(buildingId, roomId);
            if (roomIds.isEmpty()) return PageResult.empty(current, size);
            w.in(EvaluationScore::getRoomId, roomIds);
        }
        w.orderByAsc(EvaluationScore::getPeriod).orderByDesc(EvaluationScore::getTotalScore);
        IPage<EvaluationScore> p = scoreMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(p);
    }

    @Override
    public PageResult<EvaluationAward> pageAwards(Long current, Long size, String period, Long buildingId, String finalStatus) {
        LambdaQueryWrapper<EvaluationAward> w = new LambdaQueryWrapper<>();
        w.eq(period != null && !period.isBlank(), EvaluationAward::getPeriod, period)
                .eq(finalStatus != null && !finalStatus.isBlank(), EvaluationAward::getFinalStatus, finalStatus);
        if (buildingId != null) {
            List<Long> roomIds = roomIdsOfBuilding(buildingId, null);
            if (roomIds.isEmpty()) return PageResult.empty(current, size);
            w.in(EvaluationAward::getRoomId, roomIds);
        }
        w.orderByAsc(EvaluationAward::getPeriod).orderByDesc(EvaluationAward::getId);
        IPage<EvaluationAward> p = awardMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(p);
    }

    @Override
    @Transactional
    public List<EvaluationAward> generateAwards(String period) {
        if (period == null || period.isBlank()) throw new BusinessException("缺少周期 period(yyyy-MM)");
        EvaluationRule rule = ruleService.active();
        Map<String, BigDecimal> thresholds = parseThresholds(rule);

        LambdaQueryWrapper<EvaluationScore> w = new LambdaQueryWrapper<>();
        w.eq(EvaluationScore::getPeriod, period);
        List<EvaluationScore> scores = scoreMapper.selectList(w);

        List<EvaluationAward> results = new ArrayList<>();
        for (EvaluationScore s : scores) {
            String title = resolveTitle(s.getTotalScore(), thresholds);
            if (title == null) continue; // 未命中任何称号阈值，不生成
            // 同宿舍同周期只生成一条：已有则复用
            LambdaQueryWrapper<EvaluationAward> aw = new LambdaQueryWrapper<>();
            aw.eq(EvaluationAward::getRoomId, s.getRoomId()).eq(EvaluationAward::getPeriod, period).last("LIMIT 1");
            EvaluationAward award = awardMapper.selectOne(aw);
            if (award == null) {
                award = new EvaluationAward();
                award.setRoomId(s.getRoomId());
                award.setPeriod(period);
                award.setAwardName(title);
                award.setFinalStatus("PENDING");
                awardMapper.insert(award);
            } else {
                award.setAwardName(title);
                if ("PENDING".equals(award.getFinalStatus())) award.setFinalStatus("PENDING");
                awardMapper.updateById(award);
            }
            results.add(award);
        }
        return results;
    }

    @Override
    @Transactional
    public EvaluationAward reviewAward(Long awardId, String status) {
        if (!"INITIAL".equals(status) && !"FINAL".equals(status)) {
            throw new BusinessException("status 必须为 INITIAL（宿管初审）或 FINAL（辅导员终审）");
        }
        EvaluationAward award = awardMapper.selectById(awardId);
        if (award == null) throw BusinessException.notFound("评优结果不存在: " + awardId);

        UserContext ctx = UserContext.current();
        if ("FINAL".equals(status)) {
            // 终审：辅导员/管理员 + 该宿舍须属于其数据范围（楼栋维度）
            if (!UserContext.hasRole("COUNSELOR", "ADMIN")) {
                throw BusinessException.forbidden("终审需辅导员/管理员角色");
            }
        } else {
            // 初审：宿管/辅导员/管理员，且宿管仅限本楼栋
            if (!UserContext.hasRole("DORM_MANAGER", "COUNSELOR", "ADMIN")) {
                throw BusinessException.forbidden("初审需宿管/辅导员/管理员角色");
            }
            if ("DORM_MANAGER".equals(ctx.getRole())) {
                Room room = roomQueryMapper.selectById(award.getRoomId());
                if (room == null) throw BusinessException.notFound("该评优结果对应宿舍不存在");
                if (ctx.getDataScopeId() == null || !ctx.getDataScopeId().equals(room.getBuildingId())) {
                    throw BusinessException.forbidden("宿管只能审核本楼栋的评优结果");
                }
            }
        }

        award.setFinalStatus(status);
        award.setReviewerId(ctx.getUserId());
        awardMapper.updateById(award);
        return award;
    }

    @Override
    public BigDecimal roomHygieneScore(Long roomId, String period) {
        CheckRecord latest = checkRecordService.latestRecordOfRoomPeriod(roomId, period);
        if (latest == null || latest.getScore() == null) return BigDecimal.ZERO;
        return latest.getScore().setScale(2, RoundingMode.HALF_UP);
    }

    // ============================ 内部工具 ============================

    private EvaluationScore upsertScore(Long roomId, String period,
                                        BigDecimal hygiene, BigDecimal discipline,
                                        BigDecimal bonus, BigDecimal total, Integer star, EvaluationRule rule) {
        LambdaQueryWrapper<EvaluationScore> w = new LambdaQueryWrapper<>();
        w.eq(EvaluationScore::getRoomId, roomId).eq(EvaluationScore::getPeriod, period).last("LIMIT 1");
        EvaluationScore existing = scoreMapper.selectOne(w);
        EvaluationScore saved;
        if (existing == null) {
            saved = new EvaluationScore();
            saved.setRoomId(roomId);
            saved.setPeriod(period);
            saved.setHygieneScore(hygiene);
            saved.setDisciplineScore(discipline);
            saved.setBonusScore(bonus);
            saved.setTotalScore(total);
            saved.setStarLevel(star);
            scoreMapper.insert(saved);
        } else {
            existing.setHygieneScore(hygiene);
            existing.setDisciplineScore(discipline);
            existing.setBonusScore(bonus);
            existing.setTotalScore(total);
            existing.setStarLevel(star);
            scoreMapper.updateById(existing);
            saved = existing;
        }
        return saved;
    }

    private List<Long> roomIdsOfBuilding(Long buildingId, Long roomIdFilter) {
        LambdaQueryWrapper<Room> rw = new LambdaQueryWrapper<>();
        rw.eq(Room::getBuildingId, buildingId);
        if (roomIdFilter != null) rw.eq(Room::getId, roomIdFilter);
        rw.select(Room::getId);
        return roomQueryMapper.selectList(rw).stream().map(Room::getId).collect(Collectors.toList());
    }

    /** 解析 thresholds JSON：{fiveStar:90, fourStar:80, threeStar:70, twoStar:60, civilRoom:85,...}。 */
    private Map<String, BigDecimal> parseThresholds(EvaluationRule rule) {
        if (rule.getThresholds() == null || rule.getThresholds().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(rule.getThresholds(), new TypeReference<Map<String, BigDecimal>>() {
            });
        } catch (Exception e) {
            throw new BusinessException("评优规则 thresholds JSON 解析失败: " + e.getMessage());
        }
    }

    /**
     * 按阈值定星级（1~5）：总分依次与 fiveStar/fourStar/threeStar/twoStar 比较，
     * 达到某档阈值即为对应星级；全未达 → 1 星。thresholds 缺失时用 90/80/70/60 兜底。
     */
    private int resolveStar(BigDecimal total, Map<String, BigDecimal> thresholds) {
        double t = total.doubleValue();
        double five = gNum(thresholds, "fiveStar", 90);
        double four = gNum(thresholds, "fourStar", 80);
        double three = gNum(thresholds, "threeStar", 70);
        double two = gNum(thresholds, "twoStar", 60);
        if (t >= five) return 5;
        if (t >= four) return 4;
        if (t >= three) return 3;
        if (t >= two) return 2;
        return 1;
    }

    /**
     * 按阈值定称号：
     * ① 依次判断是否达到 fiveStar/fourStar/threeStar/twoStar 称号阈值，取命中的最高档为称号；
     * ② civilRoom 为"文明宿舍"达标线：若 thresholds 含 civilRoom 且总分低于它，则不授予任何称号（返回 null）。
     */
    private String resolveTitle(BigDecimal total, Map<String, BigDecimal> thresholds) {
        double t = total.doubleValue();
        String hit = null;
        // 从高星到低星：一旦命中高档就锁定（循环按 five→four→three→two 顺序，先命中的即最高档）
        for (String key : new String[]{"fiveStar", "fourStar", "threeStar", "twoStar"}) {
            if (thresholds.containsKey(key)) {
                BigDecimal th = thresholds.get(key);
                if (th != null && t >= th.doubleValue()) {
                    hit = key;
                    break;
                }
            }
        }
        // civilRoom 达标线：未达则取消称号
        if (thresholds.containsKey("civilRoom")) {
            BigDecimal civil = thresholds.get("civilRoom");
            if (civil != null && t < civil.doubleValue()) {
                hit = null;
            }
        }
        return hit;
    }

    private double gNum(Map<String, BigDecimal> m, String key, double dflt) {
        BigDecimal v = m.get(key);
        return v == null ? dflt : v.doubleValue();
    }

    private BigDecimal scale(BigDecimal b, int s) {
        return b == null ? BigDecimal.ZERO : b.setScale(s, RoundingMode.HALF_UP);
    }

    private String fmt(BigDecimal b) {
        return b == null ? "0" : b.stripTrailingZeros().toPlainString();
    }
}
