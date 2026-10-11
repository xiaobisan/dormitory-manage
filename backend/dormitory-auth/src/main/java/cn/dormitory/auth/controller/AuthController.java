package cn.dormitory.auth.controller;

import cn.dormitory.auth.dto.LoginDTO;
import cn.dormitory.auth.dto.LoginVO;
import cn.dormitory.auth.dto.MeVO;
import cn.dormitory.auth.dto.RegisterDTO;
import cn.dormitory.auth.service.AuthService;
import cn.dormitory.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口（/api/auth）。登录/注册在 SecurityConfig 放行；/me 需有效 token。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<MeVO> me() {
        return Result.ok(authService.me());
    }
}
