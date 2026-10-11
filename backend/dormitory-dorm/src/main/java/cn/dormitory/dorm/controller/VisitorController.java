package cn.dormitory.dorm.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Visitor;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.VisitorApplyDTO;
import cn.dormitory.dorm.dto.VisitorApproveDTO;
import cn.dormitory.dorm.dto.VisitorLeaveDTO;
import cn.dormitory.dorm.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 访客登记：申请 -> 审批 -> 销号。 */
@RestController
@RequestMapping("/api/dorm/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    /** 学生提交申请（PENDING）。 */
    @PostMapping("/apply")
    public Result<Visitor> apply(@RequestBody VisitorApplyDTO dto) {
        return Result.ok(visitorService.apply(dto));
    }

    /** 宿管/管理员审批（PENDING -> APPROVED / REJECTED）。 */
    @PostMapping("/approve")
    public Result<Visitor> approve(@RequestBody VisitorApproveDTO dto) {
        return Result.ok(visitorService.approve(dto));
    }

    /** 来访结束销号（APPROVED -> LEAVED）。 */
    @PostMapping("/leave")
    public Result<Visitor> leave(@RequestBody VisitorLeaveDTO dto) {
        return Result.ok(visitorService.leave(dto));
    }

    @GetMapping("/{id}")
    public Result<Visitor> getById(@PathVariable Long id) {
        return Result.ok(visitorService.getById(id));
    }

    /** 分页查访客记录。 */
    @GetMapping("/list")
    public Result<PageResult<Visitor>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status) {
        return Result.ok(visitorService.page(current, size, studentId, status));
    }
}
