package com.coldchain.transport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建运单请求DTO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建运单请求")
public class CreateWaybillRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "起始地址（仓库地址）")
    private String fromAddress;

    @NotBlank(message = "目的地址不能为空")
    @Schema(description = "目的地址（收货地址）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toAddress;
}
