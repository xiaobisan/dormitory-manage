package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Bill;
import cn.dormitory.common.entity.Payment;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.dorm.dto.PaymentDTO;
import cn.dormitory.dorm.mapper.BillMapper;
import cn.dormitory.dorm.mapper.PaymentMapper;
import cn.dormitory.dorm.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 缴费实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final BillMapper billMapper;

    @Override
    @Transactional
    public Payment pay(PaymentDTO dto) {
        if (dto == null || dto.getBillId() == null) {
            throw new BusinessException("账单 ID 不能为空");
        }
        Bill bill = billMapper.selectById(dto.getBillId());
        if (bill == null) {
            throw BusinessException.notFound("账单不存在");
        }
        if (!"UNPAID".equals(bill.getStatus())) {
            throw new BusinessException("账单当前状态为 " + bill.getStatus() + "，不是待缴状态，无法缴费");
        }
        BigDecimal amount = dto.getAmount() == null ? bill.getAmount() : dto.getAmount();

        UserContext ctx = UserContext.current();
        Payment p = new Payment();
        p.setBillId(dto.getBillId());
        p.setAmount(amount);
        p.setPaidAt(LocalDateTime.now());
        p.setPayerId(ctx != null ? ctx.getUserId() : null);
        p.setMethod(dto.getMethod());
        paymentMapper.insert(p);

        bill.setStatus("PAID");
        bill.setPaidAt(p.getPaidAt());
        billMapper.updateById(bill);
        return p;
    }

    @Override
    public Payment getById(Long id) {
        Payment p = paymentMapper.selectById(id);
        if (p == null) {
            throw BusinessException.notFound("缴费记录不存在");
        }
        return p;
    }

    @Override
    public PageResult<Payment> page(Long current, Long size, Long billId, Long payerId) {
        LambdaQueryWrapper<Payment> wrapper =
                Wrappers.<Payment>lambdaQuery()
                        .eq(billId != null, Payment::getBillId, billId)
                        .eq(payerId != null, Payment::getPayerId, payerId)
                        .orderByDesc(Payment::getPaidAt);
        IPage<Payment> page = paymentMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }
}
