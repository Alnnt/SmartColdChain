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

/**
 * 用户地址实体
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_address")
@Schema(description = "用户地址实体")
public class UserAddress extends BaseEntity {

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "收货人姓名")
    @TableField("contact_name")
    private String contactName;

    @Schema(description = "收货人电话")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "省份")
    @TableField("province")
    private String province;

    @Schema(description = "城市")
    @TableField("city")
    private String city;

    @Schema(description = "区/县")
    @TableField("district")
    private String district;

    @Schema(description = "详细地址")
    @TableField("detail")
    private String detail;

    @Schema(description = "经度")
    @TableField("longitude")
    private Double longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private Double latitude;

    @Schema(description = "是否默认地址（0-否，1-是）")
    @TableField("is_default")
    private Integer isDefault;
}
