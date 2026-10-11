package cn.dormitory.maintenance.dto;

import lombok.Data;

import java.util.Map;

/**
 * 卫生检查打分开票入参。
 * <pre>
 * {
 *   "planId": 8001,
 *   "roomId": 2001,
 *   "period": "2026-10",
 *   "itemScores": { "floor": 18, "desk": 20, "bed": 15, "doorwindow": 19, "trash": 16 },
 *   "photoUrl": "http://...",
 *   "remark": "..."
 * }
 * </pre>
 * itemScores 为 {检查项 key: 得分}，score 由后端汇总各检查项算出，避免前端乱填。
 */
@Data
public class CheckRecordSubmitDTO {
    private Long planId;
    private Long roomId;
    /** 检查周期 yyyy-MM（可选，用于标记该次检查归属周期；缺省按检查时间所在月）。 */
    private String period;
    /** 各检查项得分 {key: score}。 */
    private Map<String, Number> itemScores;
    private String photoUrl;
    private String remark;
}
