package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 退宿入参。 */
@Data
public class CheckOutDTO implements Serializable {
    /** 入住记录 ID（enrollment.id）。 */
    private Long enrollmentId;
    private String reason;
}
