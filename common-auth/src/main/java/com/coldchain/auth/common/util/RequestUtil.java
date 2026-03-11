package com.coldchain.auth.common.util;

import com.coldchain.auth.common.AuthConstants;
import com.coldchain.auth.common.JwtTokenUtil;
import jakarta.annotation.PostConstruct;import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求工具类
 */
@Configuration
public class RequestUtil {

    private static JwtTokenUtil jwtTokenUtil;

    @PostConstruct
    public void init(JwtTokenUtil jwtTokenUtil) {
        RequestUtil.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * 获取当前请求的用户ID（静态方法）
     */
    public static Long getUserId() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        var request = attributes.getRequest();
        return getUserIdFromRequest(request);
    }

    /**
     * 从请求中获取用户ID
     *
     * @param request HttpServletRequest
     * @return 用户ID
     */
    private static Long getUserIdFromRequest(HttpServletRequest request) {
        // 优先从网关传递的 Header 获取
        String userId = request.getHeader(AuthConstants.USER_ID_HEADER);
        if (StringUtils.hasText(userId)) {
            return Long.valueOf(userId);
        }
        // 否则从 Token 中解析
        String token = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        if (token != null && token.startsWith(AuthConstants.TOKEN_PREFIX)) {
            token = token.substring(AuthConstants.TOKEN_PREFIX.length());
            return jwtTokenUtil.getUserIdFromToken(token);
        }
        return null;
    }
}
