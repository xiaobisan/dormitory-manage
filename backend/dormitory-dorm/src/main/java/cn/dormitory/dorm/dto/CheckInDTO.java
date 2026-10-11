package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 入住申请入参。 */
@Data
public class CheckInDTO implements Serializable {
    private Long studentId;
    private Long roomId;
    private Long bedId;
    private String reason;
}
