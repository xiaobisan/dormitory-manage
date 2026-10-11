package cn.dormitory.dorm.service;

import cn.dormitory.common.entity.Room;
import cn.dormitory.dorm.dto.RoomDTO;

import java.util.List;

/** 宿舍管理。 */
public interface RoomService {

    Room create(RoomDTO dto);

    void update(RoomDTO dto);

    void delete(Long id);

    Room getById(Long id);

    /** 按楼栋查宿舍列表。 */
    List<Room> listByBuilding(Long buildingId);
}
