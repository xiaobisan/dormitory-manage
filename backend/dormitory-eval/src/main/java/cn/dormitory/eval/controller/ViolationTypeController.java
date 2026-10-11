package cn.dormitory.eval.controller;

import cn.dormitory.common.entity.ViolationType;
import cn.dormitory.common.result.Result;
import cn.dormitory.eval.dto.ViolationTypeDTO;
import cn.dormitory.eval.service.ViolationTypeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dormitory.common.base.PageResult;

/** 违纪类型字典（/api/eval/violation/type）。写操作需登录管理员/辅导员/宿管。 */
@RestController
@RequestMapping("/api/eval/violation/type")
@RequiredArgsConstructor
public class ViolationTypeController {

    private final ViolationTypeService typeService;

    /** 分页列表（可按 code/name 关键字过滤）。 */
    @GetMapping("/page")
    public Result<PageResult<ViolationType>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "20") long size,
                                                   @RequestParam(required = false) String keyword) {
        IPage<ViolationType> p = typeService.page(current, size, keyword);
        return Result.ok(PageResult.of(p));
    }

    /** 查单个。 */
    @GetMapping("/{id}")
    public Result<ViolationType> get(@PathVariable Long id) {
        return Result.ok(typeService.getById(id));
    }

    /** 新增。 */
    @PostMapping
    public Result<ViolationType> create(@RequestBody ViolationTypeDTO dto) {
        return Result.ok(typeService.create(dto));
    }

    /** 修改。 */
    @PutMapping("/{id}")
    public Result<ViolationType> update(@PathVariable Long id, @RequestBody ViolationTypeDTO dto) {
        return Result.ok(typeService.update(id, dto));
    }

    /** 删除（逻辑删除）。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        typeService.delete(id);
        return Result.ok();
    }
}
