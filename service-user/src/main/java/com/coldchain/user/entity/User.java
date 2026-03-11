package com.coldchain.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.coldchain.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
@Schema(description = "用户实体")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    /**
     * 密码（加密存储）
     */
    @Schema(description = "密码")
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
}
