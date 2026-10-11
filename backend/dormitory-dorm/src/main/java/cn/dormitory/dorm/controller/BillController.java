package cn.dormitory.dorm.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Bill;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.BillGenerateDTO;
import cn.dormitory.dorm.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 账单。 */
@RestController
@RequestMapping("/api/dorm/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    /** 按 period + room 汇总用量生成 UNPAID 账单。 */
    @PostMapping("/generate")
    public Result<Bill> generate(@RequestBody BillGenerateDTO dto) {
        return Result.ok(billService.generate(dto));
    }

    /** 修改账单状态（如置 PAID/OVERDUE）。 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        billService.updateStatus(id, status);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Bill> getById(@PathVariable Long id) {
        return Result.ok(billService.getById(id));
    }

    /** 分页查账单。 */
    @GetMapping("/list")
    public Result<PageResult<Bill>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status) {
        return Result.ok(billService.page(current, size, roomId, period, status));
    }
}
