package com.coldchain.ai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单解析请求
 *
 * @author coldchain
 */
@Data
@Schema(description = "订单智能解析请求")
public class OrderChatRequest {

    /**
     * 自然语言订单描述
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "自然语言订单描述", 
            example = "我需要从北京朝阳区运送100箱冷冻海鲜到上海浦东新区，要求全程-18度以下，联系人张三，电话13800138000")
    private String message;
}
