package com.coldchain.auth.common;

/**
 * 认证相关常量
 *
 * @author Alnnt
 */
public class AuthConstants {

    private AuthConstants() {
    }

    /**
     * 请求头中的Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头Authorization
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 用户ID请求头（网关传递给下游）
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名请求头（网关传递给下游）
     */
    public static final String USERNAME_HEADER = "X-Username";

    /**
     * 角色列表请求头（网关传递给下游，逗号分隔）
     */
    public static final String ROLES_HEADER = "X-Roles";

    /**
     * 仓库ID请求头（仓库管理员时由网关注入）
     */
    public static final String WAREHOUSE_ID_HEADER = "X-Warehouse-Id";

    /**
     * 请求ID
     */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * Token类型：访问令牌
     */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /**
     * Token类型：刷新令牌
     */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * Token黑名单Redis前缀
     */
    public static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";

    /**
     * 用户权限缓存前缀
     */
    public static final String USER_PERMISSION_PREFIX = "auth:user:permission:";
}
