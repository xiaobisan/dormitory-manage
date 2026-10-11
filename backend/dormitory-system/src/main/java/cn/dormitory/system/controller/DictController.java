package cn.dormitory.system.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.ViolationType;
import cn.dormitory.common.result.Result;
import cn.dormitory.system.service.DictService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 字典维护（系统管理侧）。提供违纪类型的查看/维护。
 * 其它字典（如报修分类）由各业务模块自行管理，此处不重复。
 */
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/violation-type/page")
    public Result<PageResult<ViolationType>> violationTypePage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        IPage<ViolationType> p = dictService.violationTypePage(current, size, keyword);
        return Result.ok(PageResult.of(p));
    }

    @PostMapping("/violation-type")
    public Result<Void> addViolationType(@RequestBody ViolationType type) {
        AdminGuard.requireAdmin();
        dictService.addViolationType(type);
        return Result.ok();
    }

    @PutMapping("/violation-type")
    public Result<Void> updateViolationType(@RequestBody ViolationType type) {
        AdminGuard.requireAdmin();
        dictService.updateViolationType(type);
        return Result.ok();
    }

    @DeleteMapping("/violation-type/{id}")
    public Result<Void> deleteViolationType(@PathVariable Long id) {
        AdminGuard.requireAdmin();
        dictService.deleteViolationType(id);
        return Result.ok();
    }
}
