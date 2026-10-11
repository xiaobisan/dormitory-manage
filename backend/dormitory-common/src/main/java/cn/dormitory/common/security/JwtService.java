package cn.dormitory.common.security;

import cn.dormitory.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 签发与解析。角色与数据范围写入 claims，鉴权过滤器解析后填充 {@link UserContext}。
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expireMs;

    public JwtService(@Value("${dormitory.jwt.secret:dormitory-manage-secret-key-please-change-0123456789}") String secret,
                      @Value("${dormitory.jwt.expire-ms:7200000}") long expireMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = expireMs;
    }

    public String issue(long userId, String name, String role, Long dataScopeId, String dataScopeType) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireMs);
        return Jwts.builder()
                .claims(Map.of(
                        "uid", String.valueOf(userId),
                        "name", name,
                        "role", role == null ? "" : role,
                        "dsId", dataScopeId == null ? "" : String.valueOf(dataScopeId),
                        "dsType", dataScopeType == null ? "" : dataScopeType))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /** 解析并校验签名/有效期，失败抛 {@link BusinessException#unauthorized}。 */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw BusinessException.unauthorized("登录已失效，请重新登录");
        }
    }
}
