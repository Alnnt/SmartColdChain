package com.coldchain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品创建/更新 DTO
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品创建/更新")
public class ProductDTO {

    @Schema(description = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "价格")
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0", message = "价格不能为负")
    private BigDecimal price;

    @Schema(description = "商品预览图完整URL")
    private String img;
}
