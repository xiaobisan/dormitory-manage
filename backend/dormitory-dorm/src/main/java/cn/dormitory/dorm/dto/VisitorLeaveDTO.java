package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 访客销号入参（来访结束，APPROVED -> LEAVED）。 */
@Data
public class VisitorLeaveDTO implements Serializable {
    /** 访客记录 ID。 */
    private Long visitorId;
    private String remark;
}
