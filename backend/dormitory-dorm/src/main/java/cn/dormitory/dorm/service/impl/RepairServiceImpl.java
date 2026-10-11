package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Bed;
import cn.dormitory.common.entity.Enrollment;
import cn.dormitory.common.entity.RepairOrder;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.dorm.dto.RepairAssignDTO;
import cn.dormitory.dorm.dto.RepairHandleDTO;
import cn.dormitory.dorm.dto.RepairRateDTO;
import cn.dormitory.dorm.dto.RepairSubmitDTO;
import cn.dormitory.dorm.mapper.BedMapper;
import cn.dormitory.dorm.mapper.EnrollmentMapper;
import cn.dormitory.dorm.mapper.RepairOrderMapper;
import cn.dormitory.dorm.mapper.RoomMapper;
import cn.dormitory.dorm.service.RepairService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 报修工单闭环实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    private final RepairOrderMapper repairOrderMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    @Transactional
    public RepairOrder submit(RepairSubmitDTO dto) {
        if (dto == null || dto.getRoomId() == null) {
            throw new BusinessException("宿舍不能为空");
        }
        if (roomMapper.selectById(dto.getRoomId()) == null) {
            throw BusinessException.notFound("宿舍不存在");
        }
        UserContext ctx = UserContext.current();
        Long studentId = dto.getStudentId() != null ? dto.getStudentId() : (ctx != null ? ctx.getUserId() : null);
        if (studentId == null) {
            throw new BusinessException("报修人不能为空");
        }
        RepairOrder order = new RepairOrder();
        order.setRoomId(dto.getRoomId());
        order.setStudentId(studentId);
        order.setCategory(dto.getCategory());
        order.setDescription(dto.getDescription());
        order.setPhotoUrl(dto.getPhotoUrl());
        order.setStatus("PENDING");
        order.setRemark(dto.getRemark());
        repairOrderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public RepairOrder assign(RepairAssignDTO dto) {
        RepairOrder order = requireById(dto.getOrderId());
        if (dto.getAssigneeId() == null) {
            throw new BusinessException("维修工不能为空");
        }
        // 只有待处理/处理中可派单
        if (!"PENDING".equals(order.getStatus()) && !"PROCESSING".equals(order.getStatus())) {
            throw new BusinessException("工单当前状态为 " + order.getStatus() + "，无法派单");
        }
        order.setAssigneeId(dto.getAssigneeId());
        order.setStatus("ASSIGNED");
        repairOrderMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional
    public RepairOrder handle(RepairHandleDTO dto) {
        RepairOrder order = requireById(dto.getOrderId());
        if (dto.getStatus() == null) {
            throw new BusinessException("目标状态不能为空");
        }
        String target = dto.getStatus();
        if (!List.of("PROCESSING", "DONE", "REJECTED").contains(target)) {
            throw new BusinessException("非法目标状态（应为 PROCESSING/DONE/REJECTED）");
        }
        // 校验只有被派到的维修工本人或管理员/宿管可处理
        if ("ASSIGNED".equals(order.getStatus()) && order.getAssigneeId() != null) {
            UserContext ctx = UserContext.current();
            boolean self = ctx != null && order.getAssigneeId().equals(ctx.getUserId());
            boolean adminLike = ctx != null && ("ADMIN".equals(ctx.getRole()) || "DORM_MANAGER".equals(ctx.getRole()));
            if (!self && !adminLike) {
                throw BusinessException.forbidden("只有被派单的维修工本人或宿管/管理员可处理该工单");
            }
        }
        order.setStatus(target);
        order.setRemark(dto.getRemark());
        if ("DONE".equals(target)) {
            order.setResolvedAt(LocalDateTime.now());
        }
        repairOrderMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional
    public RepairOrder rate(RepairRateDTO dto) {
        RepairOrder order = requireById(dto.getOrderId());
        if (!"DONE".equals(order.getStatus())) {
            throw new BusinessException("只有已完成的工单才可验收评价（当前：" + order.getStatus() + "）");
        }
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BusinessException("评分必须为 1-5");
        }
        order.setRating(dto.getRating());
        order.setRatingNote(dto.getRatingNote());
        repairOrderMapper.updateById(order);
        return order;
    }

    @Override
    public RepairOrder getById(Long id) {
        return requireById(id);
    }

    @Override
    public PageResult<RepairOrder> pageByRole(Long current, Long size, String status) {
        UserContext ctx = UserContext.current();
        LambdaQueryWrapper<RepairOrder> wrapper = Wrappers.<RepairOrder>lambdaQuery();
        if (ctx != null) {
            String role = ctx.getRole();
            Long uid = ctx.getUserId();
            if ("STUDENT".equals(role)) {
                // 学生：查本人（报修人）或本人宿舍的工单
                Set<Long> roomIds = myRoomIds(uid);
                Set<Long> studentIds = myStudentIds(uid);
                wrapper.and(w -> w
                        .in(RepairOrder::getStudentId, studentIds)
                        .or().in(RepairOrder::getRoomId, roomIds));
            } else if ("REPAIR_WORKER".equals(role)) {
                // 维修工：只查已派给自己的
                wrapper.eq(RepairOrder::getAssigneeId, uid);
            } else if ("DORM_MANAGER".equals(role)) {
                // 宿管：只查本楼栋
                Long buildingId = ctx.getDataScopeId();
                if (buildingId != null) {
                    List<Long> roomIds = roomsOfBuilding(buildingId);
                    if (!roomIds.isEmpty()) {
                        wrapper.in(RepairOrder::getRoomId, roomIds);
                    } else {
                        wrapper.eq(RepairOrder::getRoomId, -1L); // 无匹配
                    }
                }
            }
        }
        wrapper.eq(status != null, RepairOrder::getStatus, status)
                .orderByDesc(RepairOrder::getCreatedAt);
        IPage<RepairOrder> page = repairOrderMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    /** 指定学生（或当前用户本人 + 同宿舍室友）集合，用于学生数据隔离。 */
    private Set<Long> myStudentIds(Long uid) {
        // 本人的入住记录
        List<Enrollment> mine = enrollmentMapper.selectList(
                Wrappers.<Enrollment>lambdaQuery().eq(Enrollment::getStudentId, uid));
        Set<Long> ids = new HashSet<>();
        ids.add(uid);
        for (Enrollment e : mine) {
            // 同宿舍室友
            List<Bed> beds = bedMapper.selectList(
                    Wrappers.<Bed>lambdaQuery().eq(Bed::getRoomId, e.getRoomId()));
            for (Bed b : beds) {
                if ("OCCUPIED".equals(b.getStatus())) {
                    List<Enrollment> occ = enrollmentMapper.selectList(
                            Wrappers.<Enrollment>lambdaQuery()
                                    .eq(Enrollment::getBedId, b.getId())
                                    .eq(Enrollment::getStatus, "ACTIVE"));
                    occ.forEach(o -> ids.add(o.getStudentId()));
                }
            }
        }
        return ids;
    }

    /** 本人所在宿舍 ID 集合。 */
    private Set<Long> myRoomIds(Long uid) {
        return enrollmentMapper.selectList(
                        Wrappers.<Enrollment>lambdaQuery().eq(Enrollment::getStudentId, uid))
                .stream().map(Enrollment::getRoomId).collect(Collectors.toSet());
    }

    /** 某楼栋全部宿舍 ID。 */
    private List<Long> roomsOfBuilding(Long buildingId) {
        return roomMapper.selectList(
                        Wrappers.<Room>lambdaQuery().eq(Room::getBuildingId, buildingId))
                .stream().map(Room::getId).collect(Collectors.toList());
    }

    private RepairOrder requireById(Long id) {
        RepairOrder o = repairOrderMapper.selectById(id);
        if (o == null) {
            throw BusinessException.notFound("报修工单不存在");
        }
        return o;
    }
}
