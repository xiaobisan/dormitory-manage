package cn.dormitory.dorm.controller;

import cn.dormitory.common.entity.Usage;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.UsageDTO;
import cn.dormitory.dorm.service.UsageService;
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

import java.util.List;

/** 水电用量。 */
@RestController
@RequestMapping("/api/dorm/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    /** 录入/新增用量。 */
    @PostMapping
    public Result<Usage> create(@RequestBody UsageDTO dto) {
        return Result.ok(usageService.create(dto));
    }

    @PutMapping
    public Result<Void> update(@RequestBody UsageDTO dto) {
        usageService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        usageService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Usage> getById(@PathVariable Long id) {
        return Result.ok(usageService.getById(id));
    }

    /** 按宿舍 + 周期查用量。 */
    @GetMapping("/list")
    public Result<List<Usage>> list(
            @RequestParam Long roomId,
            @RequestParam(required = false) String period) {
        return Result.ok(usageService.listByRoomPeriod(roomId, period));
    }
}
