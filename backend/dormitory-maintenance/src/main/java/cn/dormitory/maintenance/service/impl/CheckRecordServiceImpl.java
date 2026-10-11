package cn.dormitory.maintenance.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.CheckRecord;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.maintenance.dto.CheckRecordSubmitDTO;
import cn.dormitory.maintenance.mapper.CheckRecordMapper;
import cn.dormitory.maintenance.mapper.RoomQueryMapper;
import cn.dormitory.maintenance.service.CheckRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

@Service
public class CheckRecordServiceImpl implements CheckRecordService {

    private final CheckRecordMapper recordMapper;
    private final RoomQueryMapper roomMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CheckRecordServiceImpl(CheckRecordMapper recordMapper, RoomQueryMapper roomMapper) {
        this.recordMapper = recordMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public CheckRecord submit(CheckRecordSubmitDTO dto) {
        if (dto.getRoomId() == null) throw new BusinessException("缺少 roomId");
        if (dto.getItemScores() == null || dto.getItemScores().isEmpty()) {
            throw new BusinessException("缺少检查项得分 itemScores");
        }
        // 数据隔离：宿管只查/打本楼栋宿舍
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) throw BusinessException.notFound("宿舍不存在: " + dto.getRoomId());
        scopeToOwnBuilding(room);

        // 汇总各检查项得分（score），detail 存 {key:score}
        BigDecimal total = BigDecimal.ZERO;
        Map<String, Number> scores = dto.getItemScores();
        for (Number v : scores.values()) {
            if (v == null) continue;
            total = total.add(BigDecimal.valueOf(v.doubleValue()));
        }
        total = total.setScale(1, RoundingMode.HALF_UP);

        String period = dto.getPeriod();
        if (period == null || period.isBlank()) {
            period = YearMonth.now().toString();
        }

        CheckRecord record = new CheckRecord();
        record.setPlanId(dto.getPlanId());
        record.setRoomId(dto.getRoomId());
        record.setScore(total);
        record.setDetail(toJson(scores));
        record.setPhotoUrl(dto.getPhotoUrl());
        record.setCheckedAt(LocalDateTime.now());
        record.setCheckedBy(UserContext.current().getUserId());
        record.setRemark(dto.getRemark());
        recordMapper.insert(record);
        return record;
    }

    @Override
    public PageResult<CheckRecord> page(Long current, Long size, Long buildingId, Long roomId, String period) {
        // 数据隔离：宿管只看本楼栋
        UserContext ctx = UserContext.current();
        boolean manager = UserContext.hasRole("DORM_MANAGER");
        if (manager) {
            buildingId = ctx.getDataScopeId();
        }
        Long queryBuilding = buildingId;
        if (roomId != null) {
            Room room = roomMapper.selectById(roomId);
            if (room == null) throw BusinessException.notFound("宿舍不存在: " + roomId);
            if (manager && !queryBuilding.equals(room.getBuildingId())) {
                throw BusinessException.forbidden("无权查看其他楼栋的检查记录");
            }
        }

        // 按楼栋：先取该楼栋宿舍 id，再查记录（H2 兼容，不用复杂 SQL）
        LambdaQueryWrapper<CheckRecord> w = new LambdaQueryWrapper<>();
        w.eq(roomId != null, CheckRecord::getRoomId, roomId);
        if (queryBuilding != null) {
            LambdaQueryWrapper<Room> rw = new LambdaQueryWrapper<>();
            rw.eq(Room::getBuildingId, queryBuilding).select(Room::getId);
            java.util.List<Room> rooms = roomMapper.selectList(rw);
            java.util.List<Long> roomIds = rooms.stream().map(Room::getId).toList();
            if (roomIds.isEmpty()) {
                return PageResult.empty(current, size);
            }
            w.in(CheckRecord::getRoomId, roomIds);
        }
        if (period != null && !period.isBlank()) {
            LocalDateTime periodStart = YearMonth.parse(period).atDay(1).atStartOfDay();
            LocalDateTime periodEnd = YearMonth.parse(period).plusMonths(1).atDay(1).atStartOfDay();
            w.ge(CheckRecord::getCheckedAt, periodStart).lt(CheckRecord::getCheckedAt, periodEnd);
        }
        w.orderByDesc(CheckRecord::getCheckedAt);
        IPage<CheckRecord> p = recordMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(p);
    }

    @Override
    public CheckRecord latestRecordOfRoomPeriod(Long roomId, String period) {
        LambdaQueryWrapper<CheckRecord> w = new LambdaQueryWrapper<>();
        w.eq(CheckRecord::getRoomId, roomId);
        if (period != null && !period.isBlank()) {
            LocalDateTime periodStart = YearMonth.parse(period).atDay(1).atStartOfDay();
            LocalDateTime periodEnd = YearMonth.parse(period).plusMonths(1).atDay(1).atStartOfDay();
            w.ge(CheckRecord::getCheckedAt, periodStart).lt(CheckRecord::getCheckedAt, periodEnd);
        }
        w.orderByDesc(CheckRecord::getCheckedAt).last("LIMIT 1");
        return recordMapper.selectOne(w);
    }

    /** 宿管数据隔离：检查的宿舍必须属于宿管本楼栋。 */
    private void scopeToOwnBuilding(Room room) {
        if (UserContext.hasRole("DORM_MANAGER")) {
            UserContext ctx = UserContext.current();
            if (ctx.getDataScopeId() == null || !ctx.getDataScopeId().equals(room.getBuildingId())) {
                throw BusinessException.forbidden("宿管只能检查/录入本楼栋的宿舍卫生");
            }
        }
    }

    private String toJson(Map<String, Number> scores) {
        try {
            return objectMapper.writeValueAsString(scores);
        } catch (Exception e) {
            throw new BusinessException("检查项得分序列化失败: " + e.getMessage());
        }
    }
}
