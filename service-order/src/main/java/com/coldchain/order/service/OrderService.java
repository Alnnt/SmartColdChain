package com.coldchain.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coldchain.order.dto.OrderCreateDTO;
import com.coldchain.order.dto.OrderVO;
import com.coldchain.order.entity.Order;

/**
 * 订单服务接口
 *
 * @author ColdChain
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单（分布式事务）
     *
     * @param dto 创建订单请求
     * @return 订单信息
     */
    OrderVO createOrder(OrderCreateDTO dto);

    /**
     * 根据订单ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    OrderVO getOrderById(Long orderId);

    /**
     * 根据订单编号查询订单
     *
     * @param orderNo 订单编号
     * @return 订单信息
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    Boolean cancelOrder(Long orderId);
}
