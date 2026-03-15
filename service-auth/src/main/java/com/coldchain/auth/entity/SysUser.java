package com.coldchain.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 鐢ㄦ埛瀹炰綋锛堟槧灏勭粺涓€鐢ㄦ埛琛?t_user锛?
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    /**
     * 鐢ㄦ埛绫诲瀷锛?-鏅€氱敤鎴凤紝1-绯荤粺绠＄悊鍛橈級
     */
    private Integer userType;

    /**
     * 绑定仓库ID（仓库管理员必填）
     */
    @TableField("warehouse_id")
    private Long warehouseId;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
