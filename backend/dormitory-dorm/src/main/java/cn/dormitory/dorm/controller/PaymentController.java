package cn.dormitory.dorm.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Payment;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.PaymentDTO;
import cn.dormitory.dorm.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 缴费。 */
@RestController
@RequestMapping("/api/dorm/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** 缴费：生成缴费记录并把对应账单置 PAID。 */
    @PostMapping
    public Result<Payment> pay(@RequestBody PaymentDTO dto) {
        return Result.ok(paymentService.pay(dto));
    }

    @GetMapping("/{id}")
    public Result<Payment> getById(@PathVariable Long id) {
        return Result.ok(paymentService.getById(id));
    }

    /** 分页查缴费记录。 */
    @GetMapping("/list")
    public Result<PageResult<Payment>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long billId,
            @RequestParam(required = false) Long payerId) {
        return Result.ok(paymentService.page(current, size, billId, payerId));
    }
}
