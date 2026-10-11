package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 水电用量录入入参。 */
@Data
public class UsageDTO implements Serializable {
    /** 更新/删除时必带（对应 usage.id）。 */
    private Long id;
    private Long roomId;
    /** WATER/ELECTRIC。 */
    private String type;
    /** 周期，如 2026-09。 */
    private String period;
    private BigDecimal reading;
    private BigDecimal price;
    private String remark;
}
