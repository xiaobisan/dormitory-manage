package cn.dormitory.dorm.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.RepairOrder;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.RepairAssignDTO;
import cn.dormitory.dorm.dto.RepairHandleDTO;
import cn.dormitory.dorm.dto.RepairRateDTO;
import cn.dormitory.dorm.dto.RepairSubmitDTO;
import cn.dormitory.dorm.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 报修工单闭环：提交 -> 派单 -> 维修反馈 -> 验收评价。 */
@RestController
@RequestMapping("/api/dorm/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    /** 提交报修（PENDING）。 */
    @PostMapping("/submit")
    public Result<RepairOrder> submit(@RequestBody RepairSubmitDTO dto) {
        return Result.ok(repairService.submit(dto));
    }

    /** 派单给维修工（ASSIGNED + assigneeId）。 */
    @PostMapping("/assign")
    public Result<RepairOrder> assign(@RequestBody RepairAssignDTO dto) {
        return Result.ok(repairService.assign(dto));
    }

    /** 维修工接单/反馈/完成（PROCESSING/DONE/REJECTED，DONE 写 resolvedAt）。 */
    @PostMapping("/handle")
    public Result<RepairOrder> handle(@RequestBody RepairHandleDTO dto) {
        return Result.ok(repairService.handle(dto));
    }

    /** 验收评价（仅 DONE 可评，写 rating/ratingNote）。 */
    @PostMapping("/rate")
    public Result<RepairOrder> rate(@RequestBody RepairRateDTO dto) {
        return Result.ok(repairService.rate(dto));
    }

    @GetMapping("/{id}")
    public Result<RepairOrder> getById(@PathVariable Long id) {
        return Result.ok(repairService.getById(id));
    }

    /** 按角色分页查工单（学生查本人/宿舍、维修工查已派给自己、宿管查本楼栋）。 */
    @GetMapping("/list")
    public Result<PageResult<RepairOrder>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String status) {
        return Result.ok(repairService.pageByRole(current, size, status));
    }
}
