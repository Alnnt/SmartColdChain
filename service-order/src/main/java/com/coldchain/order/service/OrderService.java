package com.coldchain.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coldchain.order.dto.OrderCreateDTO;
import com.coldchain.order.dto.OrderVO;
import com.coldchain.order.entity.Order;

import java.math.BigDecimal;

/**
 * 订单服务接口
 *
 * @author Alnnt
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单（分布式事务）
     *
     * @param dto 创建订单请求
     * @return 订单信息
     */
    OrderVO createOrder(OrderCreateDTO dto, Long userId);

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
     * 根据用户ID分页查询订单
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页大小
     * @return 订单分页列表
     */
    IPage<OrderVO> listByUserId(Long userId, Integer page, Integer pageSize);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 支付完成回调
     * 
     * 业务逻辑：
     * 1. 根据订单号查询订单
     * 2. 验证支付金额是否正确
     * 3. 更新订单状态为已支付
     * 4. 触发运单创建等后续流程
     *
     * @param orderNo    订单号
     * @param paidAmount 支付金额
     * @return 是否成功
     */
    Boolean markOrderAsPaid(String orderNo, BigDecimal paidAmount);
}
