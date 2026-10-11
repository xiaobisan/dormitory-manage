package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 报修处理入参（维修工接单/反馈/完成）。 */
@Data
public class RepairHandleDTO implements Serializable {
    /** 工单 ID。 */
    private Long orderId;
    /** 目标状态：PROCESSING（接单处理中）/ DONE（完成）/ REJECTED（驳回）。 */
    private String status;
    /** 处理说明。 */
    private String remark;
}
