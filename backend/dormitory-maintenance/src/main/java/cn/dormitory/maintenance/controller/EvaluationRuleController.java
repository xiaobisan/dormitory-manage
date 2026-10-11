package cn.dormitory.maintenance.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.EvaluationRule;
import cn.dormitory.common.result.Result;
import cn.dormitory.maintenance.service.EvaluationRuleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 评优规则接口（/api/maint/eval/rule）。 */
@RestController
@RequestMapping("/api/maint/eval/rule")
public class EvaluationRuleController {

    private final EvaluationRuleService ruleService;

    public EvaluationRuleController(EvaluationRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping("/list")
    public Result<PageResult<EvaluationRule>> list(@RequestParam(defaultValue = "1") Long current,
                                                   @RequestParam(defaultValue = "10") Long size,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) Integer active) {
        return Result.ok(ruleService.page(current, size, name, active));
    }

    @GetMapping("/{id}")
    public Result<EvaluationRule> get(@PathVariable Long id) {
        return Result.ok(ruleService.get(id));
    }

    @PostMapping
    public Result<EvaluationRule> create(@RequestBody EvaluationRule rule) {
        return Result.ok(ruleService.create(rule));
    }

    @PutMapping
    public Result<EvaluationRule> update(@RequestBody EvaluationRule rule) {
        return Result.ok(ruleService.update(rule));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return Result.ok();
    }
}
