package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 访客审批入参（宿管/管理员审批）。 */
@Data
public class VisitorApproveDTO implements Serializable {
    /** 访客记录 ID。 */
    private Long visitorId;
    /** APPROVED / REJECTED。 */
    private String status;
    private String remark;
}
