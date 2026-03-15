package com.coldchain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仓库创建/更新 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仓库创建/更新")
public class WarehouseCreateUpdateDTO {

    @NotBlank(message = "仓库名称不能为空")
    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "仓库地址不能为空")
    @Schema(description = "仓库地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotNull(message = "纬度不能为空")
    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double latitude;

    @NotNull(message = "经度不能为空")
    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double longitude;
}
