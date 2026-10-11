package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 费用单（水电费汇总）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill")
public class Bill extends BaseEntity {
    private Long roomId;
    private String period;
    private BigDecimal amount;
    /** UNPAID/PAID/OVERDUE。 */
    private String status;
    private LocalDateTime dueAt;
    private LocalDateTime paidAt;
}
