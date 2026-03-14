package com.coldchain.auth.common.config;

import com.coldchain.auth.common.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块自动配置
 *
 * @author Alnnt
 */
@Configuration
public class AuthAutoConfiguration {

    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }

//    @Bean
//    public RequestUtil requestUtil(JwtTokenUtil jwtTokenUtil) {
//        return new RequestUtil(jwtTokenUtil);
//    }
}
