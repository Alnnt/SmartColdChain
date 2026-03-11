package com.coldchain.order.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 运单响应 VO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaybillVO {

    /**
     * 运单ID
     */
    private Long id;

    /**
     * 运单编号
     */
    private String waybillNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 运单状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
