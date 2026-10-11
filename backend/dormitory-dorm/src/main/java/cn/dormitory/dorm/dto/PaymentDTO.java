package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 缴费入参。 */
@Data
public class PaymentDTO implements Serializable {
    private Long billId;
    /** 缴费金额（缺省取账单金额）。 */
    private BigDecimal amount;
    private String method;
}
