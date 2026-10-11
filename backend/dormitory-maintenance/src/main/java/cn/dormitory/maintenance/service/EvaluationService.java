package cn.dormitory.maintenance.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.EvaluationAward;
import cn.dormitory.common.entity.EvaluationScore;
import cn.dormitory.maintenance.dto.EvaluationScoreDetailDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 宿舍评优量化引擎（核心创新）。
 * <p>
 * 总分 = 卫生分×卫生权重 + (100-违纪扣分)×违纪权重 + 加分项×加分权重，
 * 再按 rules.thresholds 映射星级(1~5)/称号。权重与阈值全部来自
 * {@code EvaluationRule}（可配置），不写死。
 */
public interface EvaluationService {

    /**
     * 计算某宿舍某周期评优得分（量化明细，可追溯"怎么一步步算出来的"）。
     * 结果按 (roomId, period) upsert 到 evaluation_score。
     *
     * @param period     周期 yyyy-MM
     * @param roomId     宿舍 id
     * @param bonusScore 加分项（可配，缺省 0；预留接口）
     * @return 计算明细 DTO（卫生分/违纪分/加分/总分/星级/命中阈值/扣分明细）
     */
    EvaluationScoreDetailDTO calculateScore(String period, Long roomId, BigDecimal bonusScore);

    /** 便捷重载：加分缺省 0。 */
    EvaluationScoreDetailDTO calculateScore(String period, Long roomId);

    /** 按周期分页查评优得分（+ 楼栋过滤）。 */
    PageResult<EvaluationScore> pageScores(Long current, Long size, String period, Long buildingId, Long roomId);

    /** 按周期分页查评优结果（+ 楼栋过滤）。 */
    PageResult<EvaluationAward> pageAwards(Long current, Long size, String period, Long buildingId, String finalStatus);

    /** 生成评优结果：遍历该周期 EvaluationScore，按 thresholds 生成 EvaluationAward（PENDING）。 */
    List<EvaluationAward> generateAwards(String period);

    /**
     * 评优公示/审核。
     * status = INITIAL（宿管初审）或 FINAL（辅导员终审）。
     */
    EvaluationAward reviewAward(Long awardId, String status);

    /** 某宿舍某周期卫生分（取最近一次 CheckRecord.score，缺省 0），供前端展示。 */
    BigDecimal roomHygieneScore(Long roomId, String period);
}
