package cn.dormitory.eval.service;

import cn.dormitory.common.entity.ViolationType;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 违纪类型字典 CRUD（/api/eval/violation/type）。 */
public interface ViolationTypeService {

    IPage<ViolationType> page(long current, long size, String keyword);

    ViolationType getById(Long id);

    ViolationType create(cn.dormitory.eval.dto.ViolationTypeDTO dto);

    ViolationType update(Long id, cn.dormitory.eval.dto.ViolationTypeDTO dto);

    void delete(Long id);
}
