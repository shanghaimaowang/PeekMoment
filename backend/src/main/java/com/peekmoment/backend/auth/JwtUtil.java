package com.peekmoment.backend.auth;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.peekmoment.backend.config.JwtProperties;

/**
 * JWT 工具：双端共用一套签发/解析逻辑，用 role claim 区分身份。
 * role 取值：user（小程序用户）/ admin（管理端管理员）。
 */
@Component
public class JwtUtil {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    private final SecretKey key;
    private final long expireMinutes;

    public JwtUtil(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expireMinutes = properties.getExpireMinutes();
    }

    /** 签发 token，subject 存用户 id */
    public String issue(Long userId, String role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireMinutes * 60_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expire)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 token，无效或过期返回 null。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
