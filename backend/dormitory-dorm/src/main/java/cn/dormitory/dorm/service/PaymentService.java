package cn.dormitory.dorm.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Payment;
import cn.dormitory.dorm.dto.PaymentDTO;

/** 缴费。 */
public interface PaymentService {

    /** 缴费：生成缴费记录并把对应账单置 PAID。 */
    Payment pay(PaymentDTO dto);

    Payment getById(Long id);

    /** 分页查缴费记录。 */
    PageResult<Payment> page(Long current, Long size, Long billId, Long payerId);
}
