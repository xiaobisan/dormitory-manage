package cn.dormitory.common.security;

import cn.dormitory.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 鉴权过滤器：解析 Authorization 头中的 Bearer 令牌，填充 {@link UserContext}（业务用）
 * 与 Spring Security 的 {@code SecurityContext}（鉴权用，决定 anyRequest().authenticated() 是否放行）。
 * 白名单（登录/注册/验证码）由 {@link SecurityConfig} 放行，不进本过滤器。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                Claims c = jwtService.parse(token);
                String role = c.get("role") == null ? null : c.get("role").toString();
                UserContext ctx = new UserContext();
                ctx.setUserId(Long.parseLong(c.get("uid").toString()));
                ctx.setName(c.get("name") == null ? null : c.get("name").toString());
                ctx.setRole(role);
                String dsId = c.get("dsId") == null ? "" : c.get("dsId").toString();
                ctx.setDataScopeId(dsId.isEmpty() ? null : Long.parseLong(dsId));
                ctx.setDataScopeType(c.get("dsType") == null ? null : c.get("dsType").toString());
                UserContext.set(ctx);

                // 同步写入 Spring Security 上下文，让授权层判定为已认证
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                ctx.getUserId(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + (role == null ? "GUEST" : role))));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            chain.doFilter(req, res);
        } catch (BusinessException be) {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":401,\"message\":\"" + be.getMessage() + "\"}");
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}

