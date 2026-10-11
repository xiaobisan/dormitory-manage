package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 报修验收评价入参。 */
@Data
public class RepairRateDTO implements Serializable {
    /** 工单 ID。 */
    private Long orderId;
    /** 评分 1-5。 */
    private Integer rating;
    private String ratingNote;
}
