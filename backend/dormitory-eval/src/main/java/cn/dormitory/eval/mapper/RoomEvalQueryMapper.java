package cn.dormitory.eval.mapper;

import cn.dormitory.common.entity.Room;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/** 宿舍查询 Mapper（eval 侧做数据隔离与容量统计，实体来自 common）。
 *  命名带 Eval 后缀，避免与 maintenance 模块的同名 RoomQueryMapper 在 Bean 注册时冲突。 */
public interface RoomEvalQueryMapper extends BaseMapper<Room> {
}
