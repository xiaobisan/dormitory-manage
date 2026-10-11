package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 缴费记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment")
public class Payment extends BaseEntity {
    private Long billId;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private Long payerId;
    private String method;
}
