package cn.dormitory.eval.mapper;

import cn.dormitory.common.entity.Enrollment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/** 入住记录查询 Mapper（eval 侧做入住率/数据隔离，实体来自 common）。 */
public interface EnrollmentQueryMapper extends BaseMapper<Enrollment> {
}
