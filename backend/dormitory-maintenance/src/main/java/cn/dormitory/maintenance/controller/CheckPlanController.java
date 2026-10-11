package cn.dormitory.maintenance.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.CheckPlan;
import cn.dormitory.common.result.Result;
import cn.dormitory.maintenance.service.CheckPlanService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 卫生检查计划接口（/api/maint/check/plan）。 */
@RestController
@RequestMapping("/api/maint/check/plan")
public class CheckPlanController {

    private final CheckPlanService checkPlanService;

    public CheckPlanController(CheckPlanService checkPlanService) {
        this.checkPlanService = checkPlanService;
    }

    @GetMapping("/list")
    public Result<PageResult<CheckPlan>> list(@RequestParam(defaultValue = "1") Long current,
                                              @RequestParam(defaultValue = "10") Long size,
                                              @RequestParam(required = false) String cycle,
                                              @RequestParam(required = false) Integer active) {
        return Result.ok(checkPlanService.page(current, size, cycle, active));
    }

    @GetMapping("/{id}")
    public Result<CheckPlan> get(@PathVariable Long id) {
        return Result.ok(checkPlanService.get(id));
    }

    @PostMapping
    public Result<CheckPlan> create(@RequestBody CheckPlan plan) {
        return Result.ok(checkPlanService.create(plan));
    }

    @PutMapping
    public Result<CheckPlan> update(@RequestBody CheckPlan plan) {
        return Result.ok(checkPlanService.update(plan));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkPlanService.delete(id);
        return Result.ok();
    }
}
