package com.coldchain.order.feign.dto;

import com.coldchain.common.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建运单请求 DTO
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaybillCreateDTO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 收货地址
     */
    private Address address;

    /**
     * 联系人
     */
    private String driverName;

    /**
     * 联系电话
     */
    private String driverPhone;

    /**
     * 商品 Id
     */
    private Long productId;

    /**
     * 商品数量
     */
    private Integer count;

    /**
     * 备注
     */
    private String remark;
}
