package cn.dormitory.auth.service;

import cn.dormitory.auth.dto.LoginDTO;
import cn.dormitory.auth.dto.LoginVO;
import cn.dormitory.auth.dto.MeVO;
import cn.dormitory.auth.dto.RegisterDTO;

/** 认证服务：登录、注册、当前用户。 */
public interface AuthService {

    /** 登录：校验 status 与明文密码，签发 JWT。 */
    LoginVO login(LoginDTO dto);

    /** 开放注册，默认 role=STUDENT。 */
    void register(RegisterDTO dto);

    /** 当前登录用户（从 UserContext 取）。 */
    MeVO me();
}
