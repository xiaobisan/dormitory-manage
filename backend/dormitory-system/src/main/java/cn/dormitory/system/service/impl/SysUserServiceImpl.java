package cn.dormitory.system.service.impl;

import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.entity.SysUser;
import cn.dormitory.system.RoleEnum;
import cn.dormitory.system.dto.UserDTO;
import cn.dormitory.system.dto.UserVO;
import cn.dormitory.system.mapper.SysUserMapper;
import cn.dormitory.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统用户管理实现。
 * <p>密码：明文存取（演示用，与 auth 登录侧明文比对对齐，见契约 §9 与任务说明）。</p>
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;

    @Override
    public SysUser getByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.trim())
                .last("limit 1");
        return userMapper.selectOne(w);
    }

    @Override
    public IPage<UserVO> page(long current, long size, String role, Integer status) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .eq(role != null && !role.isBlank(), SysUser::getRole, role)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreatedAt);
        IPage<SysUser> p = userMapper.selectPage(Page.of(current, size), w);
        return p.convert(UserVO::from);
    }

    @Override
    public UserVO get(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return UserVO.from(u);
    }

    @Override
    @Transactional
    public void create(UserDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (dto.getRole() == null || !RoleEnum.isValid(dto.getRole())) {
            throw new BusinessException("非法角色：" + dto.getRole());
        }
        SysUser exist = getByUsername(dto.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在：" + dto.getUsername().trim());
        }
        SysUser u = new SysUser();
        fill(u, dto);
        u.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        userMapper.insert(u);
    }

    @Override
    @Transactional
    public void create0(SysUser user) {
        userMapper.insert(user);
    }

    @Override
    @Transactional
    public void update(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户 ID 不能为空");
        }
        SysUser u = userMapper.selectById(dto.getId());
        if (u == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPassword(dto.getPassword());
        }
        fill(u, dto);
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, int status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("status 只能是 0（停用）或 1（启用）");
        }
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw BusinessException.notFound("用户不存在");
        }
        u.setStatus(status);
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    /** 把 DTO 非空字段写入实体；密码仅在传值时覆盖。 */
    private void fill(SysUser u, UserDTO dto) {
        if (dto.getUsername() != null) u.setUsername(dto.getUsername().trim());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPassword(dto.getPassword());
        }
        if (dto.getName() != null) u.setName(dto.getName());
        if (dto.getRole() != null) {
            if (!RoleEnum.isValid(dto.getRole())) throw new BusinessException("非法角色：" + dto.getRole());
            u.setRole(dto.getRole());
        }
        if (dto.getPhone() != null) u.setPhone(dto.getPhone());
        if (dto.getDeptId() != null) u.setDeptId(dto.getDeptId());
        if (dto.getBuildingId() != null) u.setBuildingId(dto.getBuildingId());
        if (dto.getDataScopeType() != null) u.setDataScopeType(dto.getDataScopeType());
        if (dto.getDataScopeId() != null) u.setDataScopeId(dto.getDataScopeId());
        if (dto.getStatus() != null) u.setStatus(dto.getStatus());
        if (dto.getRemark() != null) u.setRemark(dto.getRemark());
    }
}
