package cn.dormitory.system.service;

import cn.dormitory.common.entity.SysUser;
import cn.dormitory.system.dto.UserDTO;
import cn.dormitory.system.dto.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface SysUserService {

    /** 按用户名查用户（含密码，供 auth 登录校验用）。 */
    SysUser getByUsername(String username);

    /** 分页列表，可按 role / status 过滤。 */
    IPage<UserVO> page(long current, long size, String role, Integer status);

    /** 查单个用户（脱敏）。 */
    UserVO get(Long id);

    /** 新增，校验 username 唯一。 */
    void create(UserDTO dto);

    /** 直接插入已构建好的实体（供 auth 注册等场景复用）。 */
    void create0(SysUser user);

    /** 修改（密码传了才更新）。 */
    void update(UserDTO dto);

    /** 停用 / 启用。 */
    void changeStatus(Long id, int status);

    /** 删除（逻辑删除）。 */
    void delete(Long id);
}
