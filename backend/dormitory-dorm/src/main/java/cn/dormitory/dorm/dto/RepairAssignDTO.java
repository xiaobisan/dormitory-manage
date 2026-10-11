package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 报修派单入参。 */
@Data
public class RepairAssignDTO implements Serializable {
    /** 工单 ID。 */
    private Long orderId;
    /** 派给哪个维修工（sys_user.id）。 */
    private Long assigneeId;
}
