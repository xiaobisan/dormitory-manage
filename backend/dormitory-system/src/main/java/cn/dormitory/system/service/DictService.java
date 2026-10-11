package cn.dormitory.system.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.ViolationType;
import cn.dormitory.system.dto.UserDTO;
import cn.dormitory.system.dto.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 字典维护（系统管理侧）。 */
public interface DictService {

    /** 违纪类型列表。 */
    IPage<ViolationType> violationTypePage(long current, long size, String keyword);

    /** 新增违纪类型。 */
    void addViolationType(ViolationType type);

    /** 修改违纪类型。 */
    void updateViolationType(ViolationType type);

    /** 删除违纪类型（逻辑删除）。 */
    void deleteViolationType(Long id);
}
