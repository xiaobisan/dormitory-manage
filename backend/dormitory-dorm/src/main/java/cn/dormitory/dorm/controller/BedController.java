package cn.dormitory.dorm.controller;

import cn.dormitory.common.entity.Bed;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.BedDTO;
import cn.dormitory.dorm.service.BedService;
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

/** 床位管理。 */
@RestController
@RequestMapping("/api/dorm/bed")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @PostMapping
    public Result<Bed> create(@RequestBody BedDTO dto) {
        return Result.ok(bedService.create(dto));
    }

    @PutMapping
    public Result<Void> update(@RequestBody BedDTO dto) {
        bedService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bedService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Bed> getById(@PathVariable Long id) {
        return Result.ok(bedService.getById(id));
    }

    /** 按宿舍查床位。 */
    @GetMapping("/list")
    public Result<List<Bed>> listByRoom(@RequestParam Long roomId) {
        return Result.ok(bedService.listByRoom(roomId));
    }

    /** 修改床位状态（EMPTY/OCCUPIED/REPAIR/LOCKED）。 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        bedService.updateStatus(id, status);
        return Result.ok();
    }
}
