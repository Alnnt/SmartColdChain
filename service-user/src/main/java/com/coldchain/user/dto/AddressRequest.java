package com.coldchain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址请求DTO（创建/编辑）
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "地址请求")
public class AddressRequest {

    @Schema(description = "地址ID（编辑时必填）")
    private Long id;

    @NotBlank(message = "收货人姓名不能为空")
    @Schema(description = "收货人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactName;

    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "收货人电话", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPhone;

    @NotBlank(message = "省份不能为空")
    @Schema(description = "省份", requiredMode = Schema.RequiredMode.REQUIRED)
    private String province;

    @NotBlank(message = "城市不能为空")
    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @NotBlank(message = "区/县不能为空")
    @Schema(description = "区/县", requiredMode = Schema.RequiredMode.REQUIRED)
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detail;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "是否设为默认地址")
    private Boolean isDefault;

    @Schema(description = "标签（家、公司等）")
    private String tag;
}
