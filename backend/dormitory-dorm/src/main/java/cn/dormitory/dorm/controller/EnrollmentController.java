package cn.dormitory.dorm.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Enrollment;
import cn.dormitory.common.result.Result;
import cn.dormitory.dorm.dto.CheckInDTO;
import cn.dormitory.dorm.dto.CheckOutDTO;
import cn.dormitory.dorm.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 入住/退宿。 */
@RestController
@RequestMapping("/api/dorm/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /** 入住：校验床位空闲 -> 生成 ACTIVE 入住记录 -> 床位置 OCCUPIED。 */
    @PostMapping("/check-in")
    public Result<Enrollment> checkIn(@RequestBody CheckInDTO dto) {
        return Result.ok(enrollmentService.checkIn(dto));
    }

    /** 退宿：入住记录置 CHECKED_OUT -> 床位置 EMPTY。 */
    @PostMapping("/check-out")
    public Result<Enrollment> checkOut(@RequestBody CheckOutDTO dto) {
        return Result.ok(enrollmentService.checkOut(dto));
    }

    /** 分页查入住记录。 */
    @GetMapping("/list")
    public Result<PageResult<Enrollment>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyActive) {
        return Result.ok(enrollmentService.page(current, size, studentId, buildingId, onlyActive));
    }

    @GetMapping("/{id}")
    public Result<Enrollment> getById(@PathVariable Long id) {
        return Result.ok(enrollmentService.getById(id));
    }
}
