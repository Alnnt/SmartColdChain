package com.coldchain.auth.service.impl;

import com.coldchain.auth.common.AuthConstants;
import com.coldchain.auth.common.JwtTokenUtil;
import com.coldchain.auth.dto.*;
import com.coldchain.auth.entity.SysPermission;
import com.coldchain.auth.entity.SysRole;
import com.coldchain.auth.entity.SysUser;
import com.coldchain.auth.mapper.SysPermissionMapper;
import com.coldchain.auth.mapper.SysRoleMapper;
import com.coldchain.auth.mapper.SysUserMapper;
import com.coldchain.auth.service.AuthService;
import com.coldchain.common.exception.BusinessException;
import com.coldchain.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 *
 * @author ColdChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            user = userMapper.selectByPhone(request.getUsername());
        }

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }

        // 校验状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED, "账号已被禁用");
        }

        // 获取角色和权限
        List<SysRole> roles = roleMapper.selectByUserId(user.getId());
        List<SysPermission> permissions = permissionMapper.selectByUserId(user.getId());

        List<String> roleCodes = roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());

        List<String> permissionCodes = permissions.stream()
                .map(SysPermission::getCode)
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toList());

        // 生成Token
        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername(), roleCodes, permissionCodes);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: username={}", user.getUsername());

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roleCodes)
                .permissions(permissionCodes)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证刷新Token
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED, "刷新令牌已过期");
        }

        if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "无效的刷新令牌");
        }

        // 获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED, "用户不存在或已被禁用");
        }

        // 获取角色和权限
        List<SysRole> roles = roleMapper.selectByUserId(userId);
        List<SysPermission> permissions = permissionMapper.selectByUserId(userId);

        List<String> roleCodes = roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());

        List<String> permissionCodes = permissions.stream()
                .map(SysPermission::getCode)
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toList());

        // 生成新Token
        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username, roleCodes, permissionCodes);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);

        log.info("Token刷新成功: username={}", username);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 获取角色和权限
        List<SysRole> roles = roleMapper.selectByUserId(userId);
        List<SysPermission> permissions = permissionMapper.selectByUserId(userId);

        List<String> roleCodes = roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());

        List<String> permissionCodes = permissions.stream()
                .map(SysPermission::getCode)
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toList());

        return UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .deptId(user.getDeptId())
                .roles(roleCodes)
                .permissions(permissionCodes)
                .build();
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtTokenUtil.validateToken(token)) {
            // 将Token加入黑名单
            String key = AuthConstants.TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", jwtTokenUtil.getAccessTokenExpiration(), TimeUnit.SECONDS);
            log.info("用户登出，Token已加入黑名单");
        }
    }

    @Override
    public boolean validateToken(String token) {
        // 检查Token是否在黑名单中
        String key = AuthConstants.TOKEN_BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }
        return jwtTokenUtil.validateToken(token);
    }
}
