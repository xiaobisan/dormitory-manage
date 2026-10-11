package cn.dormitory.maintenance.mapper;

import cn.dormitory.common.entity.Room;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/** 宿舍查询 Mapper（维护侧查楼栋归属做数据隔离，Room 实体来自 common）。 */
public interface RoomQueryMapper extends BaseMapper<Room> {
}
