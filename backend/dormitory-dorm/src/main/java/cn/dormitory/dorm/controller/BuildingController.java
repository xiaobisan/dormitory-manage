package cn.dormitory.dorm.controller;

import cn.dormitory.common.entity.Building;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.BuildingDTO;
import cn.dormitory.dorm.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 楼栋管理。 */
@RestController
@RequestMapping("/api/dorm/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping
    public Result<Building> create(@RequestBody BuildingDTO dto) {
        return Result.ok(buildingService.create(dto));
    }

    @PutMapping
    public Result<Void> update(@RequestBody BuildingDTO dto) {
        buildingService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Building> getById(@PathVariable Long id) {
        return Result.ok(buildingService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<Building>> listAll() {
        return Result.ok(buildingService.listAll());
    }
}
