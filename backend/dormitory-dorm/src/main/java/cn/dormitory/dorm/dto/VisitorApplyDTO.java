package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 访客登记入参（学生提交申请，状态 PENDING）。 */
@Data
public class VisitorApplyDTO implements Serializable {
    private Long studentId;
    private String name;
    private String relation;
    private String phone;
    private LocalDateTime visitFrom;
    private LocalDateTime visitTo;
    private String remark;
}
