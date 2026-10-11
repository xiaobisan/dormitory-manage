package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 水电用量。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("usage")
public class Usage extends BaseEntity {
    private Long roomId;
    /** WATER/ELECTRIC。 */
    private String type;
    /** 周期，如 2026-09。 */
    private String period;
    private BigDecimal reading;
    private BigDecimal price;
}
