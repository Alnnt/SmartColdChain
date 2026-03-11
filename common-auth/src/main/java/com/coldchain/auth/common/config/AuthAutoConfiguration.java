package com.coldchain.auth.common.config;

import com.coldchain.auth.common.JwtTokenUtil;
import com.coldchain.auth.common.util.RequestUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块自动配置
 *
 * @author ColdChain
 */
@Configuration
public class AuthAutoConfiguration {

    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }

    @Bean
    public RequestUtil requestUtil() { return new RequestUtil(); }
}
