package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.entity.Bed;
import cn.dormitory.common.entity.Enrollment;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.base.PageResult;
import cn.dormitory.dorm.dto.CheckInDTO;
import cn.dormitory.dorm.dto.CheckOutDTO;
import cn.dormitory.dorm.mapper.BedMapper;
import cn.dormitory.dorm.mapper.EnrollmentMapper;
import cn.dormitory.dorm.mapper.RoomMapper;
import cn.dormitory.dorm.service.EnrollmentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 入住/退宿实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public Enrollment checkIn(CheckInDTO dto) {
        if (dto == null || dto.getStudentId() == null || dto.getBedId() == null) {
            throw new BusinessException("学生与床位不能为空");
        }
        Bed bed = bedMapper.selectById(dto.getBedId());
        if (bed == null) {
            throw BusinessException.notFound("床位不存在");
        }
        // 校验床位空闲
        if (!"EMPTY".equals(bed.getStatus())) {
            throw new BusinessException("床位已被占用或不可用（当前状态：" + bed.getStatus() + "）");
        }
        // 校验该学生没有有效入住记录
        Long active = enrollmentMapper.selectCount(
                Wrappers.<Enrollment>lambdaQuery()
                        .eq(Enrollment::getStudentId, dto.getStudentId())
                        .eq(Enrollment::getStatus, "ACTIVE"));
        if (active != null && active > 0) {
            throw new BusinessException("该学生已有有效入住记录，不能重复入住");
        }
        Room room = roomMapper.selectById(bed.getRoomId());
        if (room == null) {
            throw BusinessException.notFound("床位所属宿舍不存在");
        }

        Enrollment e = new Enrollment();
        e.setStudentId(dto.getStudentId());
        e.setBuildingId(room.getBuildingId());
        e.setRoomId(bed.getRoomId());
        e.setBedId(bed.getId());
        e.setCheckInAt(LocalDateTime.now());
        e.setStatus("ACTIVE");
        e.setReason(dto.getReason());
        enrollmentMapper.insert(e);

        // 床位置 OCCUPIED，宿舍置 IN_USE
        bed.setStatus("OCCUPIED");
        bedMapper.updateById(bed);
        room.setStatus("IN_USE");
        roomMapper.updateById(room);
        return e;
    }

    @Override
    @Transactional
    public Enrollment checkOut(CheckOutDTO dto) {
        if (dto == null || dto.getEnrollmentId() == null) {
            throw new BusinessException("入住记录 ID 不能为空");
        }
        Enrollment e = requireById(dto.getEnrollmentId());
        if (!"ACTIVE".equals(e.getStatus())) {
            throw new BusinessException("该入住记录不是 ACTIVE 状态，无法退宿");
        }
        e.setStatus("CHECKED_OUT");
        e.setCheckOutAt(LocalDateTime.now());
        e.setReason(dto.getReason());
        enrollmentMapper.updateById(e);

        Bed bed = bedMapper.selectById(e.getBedId());
        if (bed != null) {
            bed.setStatus("EMPTY");
            bedMapper.updateById(bed);
        }
        return e;
    }

    @Override
    public Enrollment getById(Long id) {
        return requireById(id);
    }

    @Override
    public PageResult<Enrollment> page(Long current, Long size, Long studentId, Long buildingId, Boolean onlyActive) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Enrollment> wrapper =
                Wrappers.<Enrollment>lambdaQuery()
                        .eq(studentId != null, Enrollment::getStudentId, studentId)
                        .eq(buildingId != null, Enrollment::getBuildingId, buildingId)
                        .eq(Boolean.TRUE.equals(onlyActive), Enrollment::getStatus, "ACTIVE")
                        .orderByDesc(Enrollment::getCheckInAt);
        IPage<Enrollment> page = enrollmentMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    private Enrollment requireById(Long id) {
        Enrollment e = enrollmentMapper.selectById(id);
        if (e == null) {
            throw BusinessException.notFound("入住记录不存在");
        }
        return e;
    }
}
