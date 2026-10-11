package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 报修工单。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair_order")
public class RepairOrder extends BaseEntity {
    private Long roomId;
    private Long studentId;
    private String category;
    private String description;
    private String photoUrl;
    /** PENDING/ASSIGNED/PROCESSING/DONE/REJECTED。 */
    private String status;
    private Long assigneeId;
    private LocalDateTime resolvedAt;
    private Integer rating;
    private String ratingNote;
}
