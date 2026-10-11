package cn.dormitory.maintenance.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.EvaluationAward;
import cn.dormitory.common.entity.EvaluationScore;
import cn.dormitory.common.result.Result;
import cn.dormitory.maintenance.dto.EvaluationScoreDetailDTO;
import cn.dormitory.maintenance.service.EvaluationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** 评优量化引擎接口（/api/maint/eval）。 */
@RestController
@RequestMapping("/api/maint/eval")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    /**
     * 计算得分：返回完整量化明细（卫生分/违纪分/加分/总分/星级/命中阈值/违纪扣分明细），
     * 同时 upsert 到 evaluation_score。
     * bonusScore 可选（加分项，预留，缺省 0）。
     */
    @GetMapping("/score/calculate")
    public Result<EvaluationScoreDetailDTO> calculate(@RequestParam String period,
                                                      @RequestParam Long roomId,
                                                      @RequestParam(required = false, defaultValue = "0") BigDecimal bonusScore) {
        return Result.ok(evaluationService.calculateScore(period, roomId, bonusScore));
    }

    /** 查评优得分（按周期+楼栋，分页）。 */
    @GetMapping("/score/list")
    public Result<PageResult<EvaluationScore>> scoreList(@RequestParam(defaultValue = "1") Long current,
                                                          @RequestParam(defaultValue = "10") Long size,
                                                          @RequestParam(required = false) String period,
                                                          @RequestParam(required = false) Long buildingId,
                                                          @RequestParam(required = false) Long roomId) {
        return Result.ok(evaluationService.pageScores(current, size, period, buildingId, roomId));
    }

    /** 生成评优结果：遍历该周期得分，按阈值生成 PENDING 评语结果。 */
    @PostMapping("/award/generate")
    public Result<List<EvaluationAward>> generate(@RequestParam String period) {
        return Result.ok(evaluationService.generateAwards(period));
    }

    /** 查评优结果（按周期+楼栋+状态，分页）。 */
    @GetMapping("/award/list")
    public Result<PageResult<EvaluationAward>> awardList(@RequestParam(defaultValue = "1") Long current,
                                                          @RequestParam(defaultValue = "10") Long size,
                                                          @RequestParam(required = false) String period,
                                                          @RequestParam(required = false) Long buildingId,
                                                          @RequestParam(required = false) String finalStatus) {
        return Result.ok(evaluationService.pageAwards(current, size, period, buildingId, finalStatus));
    }

    /** 评优公示/审核：INITIAL 宿管初审 → FINAL 辅导员终审。 */
    @PostMapping("/award/review")
    public Result<EvaluationAward> review(@RequestBody AwardReviewDTO dto) {
        return Result.ok(evaluationService.reviewAward(dto.getAwardId(), dto.getStatus()));
    }

    /** 审核入参。 */
    public static class AwardReviewDTO {
        private Long awardId;
        private String status;

        public Long getAwardId() { return awardId; }
        public void setAwardId(Long awardId) { this.awardId = awardId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
