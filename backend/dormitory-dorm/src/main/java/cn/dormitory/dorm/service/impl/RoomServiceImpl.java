package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.entity.Building;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.dorm.dto.RoomDTO;
import cn.dormitory.dorm.mapper.BuildingMapper;
import cn.dormitory.dorm.mapper.RoomMapper;
import cn.dormitory.dorm.service.RoomService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 宿舍管理实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;

    @Override
    @Transactional
    public Room create(RoomDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getRoomNo())) {
            throw new BusinessException("宿舍号不能为空");
        }
        requireBuilding(dto.getBuildingId());
        // 同楼栋同宿舍号唯一
        Long count = roomMapper.selectCount(
                Wrappers.<Room>lambdaQuery()
                        .eq(Room::getBuildingId, dto.getBuildingId())
                        .eq(Room::getRoomNo, dto.getRoomNo()));
        if (count != null && count > 0) {
            throw new BusinessException("该楼栋下宿舍号已存在");
        }
        Room r = new Room();
        r.setBuildingId(dto.getBuildingId());
        r.setFloor(dto.getFloor());
        r.setRoomNo(dto.getRoomNo());
        r.setCapacity(dto.getCapacity() == null ? 4 : dto.getCapacity());
        r.setHeadStudent(dto.getHeadStudent());
        r.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "IN_USE");
        r.setRemark(dto.getRemark());
        roomMapper.insert(r);
        return r;
    }

    @Override
    @Transactional
    public void update(RoomDTO dto) {
        Room exist = requireById(dto.getId());
        requireBuilding(dto.getBuildingId());
        exist.setBuildingId(dto.getBuildingId());
        exist.setFloor(dto.getFloor());
        if (StringUtils.hasText(dto.getRoomNo())) {
            exist.setRoomNo(dto.getRoomNo());
        }
        if (dto.getCapacity() != null) {
            exist.setCapacity(dto.getCapacity());
        }
        exist.setHeadStudent(dto.getHeadStudent());
        if (StringUtils.hasText(dto.getStatus())) {
            exist.setStatus(dto.getStatus());
        }
        exist.setRemark(dto.getRemark());
        roomMapper.updateById(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireById(id);
        roomMapper.deleteById(id);
    }

    @Override
    public Room getById(Long id) {
        return requireById(id);
    }

    @Override
    public List<Room> listByBuilding(Long buildingId) {
        requireBuilding(buildingId);
        // 宿管数据范围校验：只能查自己楼栋
        UserContext ctx = UserContext.current();
        if (ctx != null && "DORM_MANAGER".equals(ctx.getRole())
                && ctx.getDataScopeId() != null && !ctx.getDataScopeId().equals(buildingId)) {
            throw BusinessException.forbidden("无权查看其他楼栋的宿舍");
        }
        return roomMapper.selectList(
                Wrappers.<Room>lambdaQuery()
                        .eq(Room::getBuildingId, buildingId)
                        .orderByAsc(Room::getFloor)
                        .orderByAsc(Room::getRoomNo));
    }

    private void requireBuilding(Long buildingId) {
        if (buildingId == null) {
            throw new BusinessException("所属楼栋不能为空");
        }
        if (buildingMapper.selectById(buildingId) == null) {
            throw BusinessException.notFound("楼栋不存在");
        }
    }

    private Room requireById(Long id) {
        Room r = roomMapper.selectById(id);
        if (r == null) {
            throw BusinessException.notFound("宿舍不存在");
        }
        return r;
    }
}
