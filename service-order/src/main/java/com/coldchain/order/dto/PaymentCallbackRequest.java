package com.coldchain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付完成回调请求DTO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付完成回调请求")
public class PaymentCallbackRequest {

    @NotBlank(message = "订单号不能为空")
    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @NotNull(message = "支付金额不能为空")
    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal paidAmount;

    @NotNull(message = "支付时间不能为空")
    @Schema(description = "支付时间戳（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long paymentTime;

    @Schema(description = "支付方式（支付宝/微信/银行卡等）")
    private String paymentMethod;

    @Schema(description = "支付交易号")
    private String transactionId;

    @Schema(description = "备注")
    private String remark;
}
