package cn.dormitory.eval.mapper;

import cn.dormitory.common.entity.ViolationType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/** 违纪类型字典 Mapper（eval 侧维护字典 CRUD）。
 *  命名带 Eval 后缀，避免与 system 模块的同名 ViolationTypeMapper 在 Bean 注册时冲突。 */
public interface ViolationTypeEvalMapper extends BaseMapper<ViolationType> {
}
