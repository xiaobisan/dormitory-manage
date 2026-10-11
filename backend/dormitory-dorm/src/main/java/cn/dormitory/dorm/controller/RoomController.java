package cn.dormitory.dorm.controller;

import cn.dormitory.common.entity.Room;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.RoomDTO;
import cn.dormitory.dorm.service.RoomService;
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

/** 宿舍管理。 */
@RestController
@RequestMapping("/api/dorm/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public Result<Room> create(@RequestBody RoomDTO dto) {
        return Result.ok(roomService.create(dto));
    }

    @PutMapping
    public Result<Void> update(@RequestBody RoomDTO dto) {
        roomService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        return Result.ok(roomService.getById(id));
    }

    /** 按楼栋查宿舍列表。 */
    @GetMapping("/list")
    public Result<List<Room>> listByBuilding(@RequestParam Long buildingId) {
        return Result.ok(roomService.listByBuilding(buildingId));
    }
}
