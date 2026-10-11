package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 报修提交入参。 */
@Data
public class RepairSubmitDTO implements Serializable {
    private Long roomId;
    /** 报修人（可空，缺省取当前登录用户）。 */
    private Long studentId;
    private String category;
    private String description;
    private String photoUrl;
    private String remark;
}
