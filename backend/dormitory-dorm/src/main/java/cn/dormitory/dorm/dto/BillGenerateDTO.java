package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 账单生成入参（按 period + room 汇总用量生成账单）。 */
@Data
public class BillGenerateDTO implements Serializable {
    private Long roomId;
    /** 周期，如 2026-09。 */
    private String period;
    /** 账单截止缴纳时间（可空）。 */
    private java.time.LocalDateTime dueAt;
}
