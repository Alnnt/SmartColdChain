package com.coldchain.transport.dto;

import lombok.Data;

/**
 * 创建运单请求（与订单服务 Feign WaybillCreateDTO 结构一致，便于对接）
 */
@Data
public class WaybillCreateApiDTO {

    private Long orderId;
    private String orderNo;
    private Long addressId;
    private Long productId;
    private Integer count;
}
