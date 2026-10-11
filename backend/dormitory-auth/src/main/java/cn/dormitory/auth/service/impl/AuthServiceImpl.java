package cn.dormitory.auth.service.impl;

import cn.dormitory.auth.dto.LoginDTO;
import cn.dormitory.auth.dto.LoginVO;
import cn.dormitory.auth.dto.MeVO;
import cn.dormitory.auth.dto.RegisterDTO;
import cn.dormitory.auth.service.AuthService;
import cn.dormitory.common.entity.SysUser;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.JwtService;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.system.RoleEnum;
import cn.dormitory.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现。
 * <p>密码：明文存取与比对（演示用，与 system 侧 SysUserServiceImpl 对齐）。
 * JWT：由 common 的 {@link JwtService} 签发，角色与数据范围写入 claims。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService userService;
    private final JwtService jwtService;

    @Override
    public LoginVO login(LoginDTO dto) {
        if (dto == null || dto.getUsername() == null || dto.getPassword() == null) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null
                || user.getStatus() == null || user.getStatus() != 1
                || !user.getPassword().equals(dto.getPassword())) {
            // 统一错误信息，避免泄露"用户是否存在"
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        String token = jwtService.issue(
                user.getId(), user.getName(), user.getRole(),
                user.getDataScopeId(), user.getDataScopeType());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setName(user.getName());
        vo.setRole(user.getRole());
        vo.setBuildingId(user.getBuildingId());
        vo.setDeptId(user.getDeptId());
        return vo;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (dto == null || dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException("用户名/密码不能为空");
        }
        if (userService.getByUsername(dto.getUsername().trim()) != null) {
            throw new BusinessException("用户名已存在：" + dto.getUsername().trim());
        }
        SysUser u = new SysUser();
        u.setUsername(dto.getUsername().trim());
        u.setPassword(dto.getPassword());
        u.setName(dto.getName() == null || dto.getName().isBlank()
                ? dto.getUsername().trim() : dto.getName().trim());
        u.setPhone(dto.getPhone());
        String role = dto.getRole();
        u.setRole(role == null || role.isBlank() ? RoleEnum.STUDENT : role);
        u.setStatus(1);
        // 开放注册：默认学生，无数据范围
        userService.create0(u);
    }

    @Override
    public MeVO me() {
        UserContext ctx = UserContext.current();
        if (ctx == null || ctx.getUserId() == null) {
            throw BusinessException.unauthorized("未登录");
        }
        SysUser user = userService.get(ctx.getUserId());
        MeVO vo = new MeVO();
        vo.setUserId(ctx.getUserId());
        vo.setName(user.getName());
        vo.setRole(user.getRole());
        vo.setBuildingId(user.getBuildingId());
        vo.setDeptId(user.getDeptId());
        return vo;
    }
}
