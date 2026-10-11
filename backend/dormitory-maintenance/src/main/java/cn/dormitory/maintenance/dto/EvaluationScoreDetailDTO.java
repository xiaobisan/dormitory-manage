package cn.dormitory.maintenance.dto;

import cn.dormitory.common.entity.EvaluationScore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评优量化引擎计算明细（"怎么一步步算出来的"，可追溯）。
 * 对应实体 {@link EvaluationScore} 之外额外带规则、权重、阈值与违纪扣分明细。
 */
@Data
public class EvaluationScoreDetailDTO {

    private Long roomId;
    private String period;
    /** 宿舍号（联查 room 冗余展示）。 */
    private String roomNo;

    /** 采用的评优规则。 */
    private Long ruleId;
    private String ruleName;
    private BigDecimal hygieneWeight;
    private BigDecimal disciplineWeight;
    private BigDecimal bonusWeight;

    /** ① 卫生分（CheckRecord 最近一次得分，缺省 0）。 */
    private BigDecimal hygieneScore;

    /** ② 违纪总扣分（violation sum），违纪分 = 100 - 违纪扣分。 */
    private BigDecimal violationDeduction;
    private BigDecimal disciplineScore;
    /** 违纪扣分明细（哪些违纪、各扣多少，便于追溯）。 */
    private List<ViolationDeductionRow> violationDetails;

    /** ③ 加分项（可配，缺省 0，预留）。 */
    private BigDecimal bonusScore;

    /** 总分 = 卫生分×卫生权重 + 违纪分×违纪权重 + 加分×加分权重。 */
    private BigDecimal totalScore;
    /** 命中阈值后的星级（1~5）。 */
    private Integer starLevel;
    /** 命中的称号（thresholds 中的键，如 fiveStar/civilRoom）。 */
    private String hitTitle;
    /** 计算说明（人话版公式），前端可直接展示。 */
    private String calculationTrace;

    /** 写入/更新后的得分实体（upsert 结果）。 */
    private EvaluationScore savedScore;

    /** 单条违纪扣分明细。 */
    @Data
    public static class ViolationDeductionRow {
        private Long violationId;
        private String typeName;
        private String description;
        private BigDecimal deduction;
    }
}
