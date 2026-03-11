package com.coldchain.auth.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类（公共）
 * 
 * 网关和认证服务共用此类
 *
 * @author Alnnt
 */
@Slf4j
public class JwtTokenUtil {

    @Value("${jwt.secret:Y29sZGNoYWluLXNlY3JldC1rZXktbXVzdC1iZS1hdC1sZWFzdC0yNTYtYml0cy1sb25nLWZvci1qd3Q=}")
    private String secret;

    @Getter
    @Value("${jwt.access-token-expiration:7200}")
    private Long accessTokenExpiration;

    @Getter
    @Value("${jwt.refresh-token-expiration:604800}")
    private Long refreshTokenExpiration;

    /**
     * 获取密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问Token
     */
    public String generateAccessToken(Long userId, String username, List<String> roles, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("type", AuthConstants.TOKEN_TYPE_ACCESS);

        return buildToken(claims, username, accessTokenExpiration);
    }

    /**
     * 生成访问Token（简化版，用于普通用户）
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", AuthConstants.TOKEN_TYPE_ACCESS);

        return buildToken(claims, username, accessTokenExpiration);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", AuthConstants.TOKEN_TYPE_REFRESH);

        return buildToken(claims, username, refreshTokenExpiration);
    }

    /**
     * 构建Token
     */
    private String buildToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return (Long) userId;
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    /**
     * 获取Token类型
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("type", String.class);
        }
        return null;
    }

    /**
     * 获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("roles", List.class);
        }
        return null;
    }

    /**
     * 获取权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("permissions", List.class);
        }
        return null;
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证是否为刷新Token
     */
    public boolean isRefreshToken(String token) {
        String type = getTokenType(token);
        return AuthConstants.TOKEN_TYPE_REFRESH.equals(type);
    }

    /**
     * 验证是否为访问Token
     */
    public boolean isAccessToken(String token) {
        String type = getTokenType(token);
        return AuthConstants.TOKEN_TYPE_ACCESS.equals(type);
    }
}
