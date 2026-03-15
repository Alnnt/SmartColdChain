package com.coldchain.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.coldchain.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品实体
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
@Schema(description = "商品实体")
public class Product extends BaseEntity {

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "价格")
    private BigDecimal price;

    /**
     * 商品预览图完整 URL
     */
    @Schema(description = "商品预览图URL")
    private String img;
}
