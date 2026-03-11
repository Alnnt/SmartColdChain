package com.coldchain.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coldchain.auth.common.util.RequestUtil;
import com.coldchain.common.exception.BusinessException;
import com.coldchain.common.result.Result;
import com.coldchain.common.result.ResultCode;
import com.coldchain.order.dto.OrderCreateDTO;
import com.coldchain.order.dto.OrderVO;
import com.coldchain.order.entity.Order;
import com.coldchain.order.entity.enums.OrderStatus;
import com.coldchain.order.feign.InventoryClient;
import com.coldchain.order.feign.TransportClient;
import com.coldchain.order.feign.dto.WaybillCreateDTO;
import com.coldchain.order.feign.dto.WaybillVO;
import com.coldchain.order.mapper.OrderMapper;
import com.coldchain.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 订单服务实现
 *
 * @author ColdChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final InventoryClient inventoryClient;
    private final TransportClient transportClient;

    /**
     * 雪花算法 ID生成器
     */
    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 创建订单（分布式事务）
     * 流程：创建订单 -> 扣减库存 -> 创建运单 -> 更新订单运单ID
     */
    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateDTO dto, Long userId) {
        log.info("开始创建订单: userId={}, productId={}, count={}",
                userId, dto.getProductId(), dto.getProductCount());

        // 1. 生成订单编号
        String orderNo = generateOrderNo();

        // 2. 创建订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .productId(dto.getProductId())
                .productCount(dto.getProductCount())
                .amount(dto.getAmount())
                .status(OrderStatus.PENDING_PAYMENT.getCode())
                .addressId(dto.getAddressId())
                .build();

        boolean saved = this.save(order);
        if (!saved) {
            throw new BusinessException(ResultCode.FAIL, "订单创建失败");
        }
        log.info("订单创建成功: orderId={}, orderNo={}", order.getId(), orderNo);

        // 3. 调用库存服务扣减库存
        Result<Boolean> inventoryResult = inventoryClient.decreaseStock(dto.getProductId(), dto.getProductCount());
        if (!inventoryResult.isSuccess()) {
            log.error("库存扣减失败: {}", inventoryResult.getMessage());
            throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH, inventoryResult.getMessage());
        }
        log.info("库存扣减成功: productId={}, count={}", dto.getProductId(), dto.getProductCount());

        // 4. 调用运输服务创建运单
        WaybillCreateDTO waybillDTO = WaybillCreateDTO.builder()
                .orderId(order.getId())
                .orderNo(orderNo)
                .addressId(dto.getAddressId())
                .productId(dto.getProductId())
                .count(dto.getProductCount())
                .build();

        Result<WaybillVO> transportResult = transportClient.createWaybill(waybillDTO);
        if (!transportResult.isSuccess()) {
            log.error("运单创建失败: {}", transportResult.getMessage());
            throw new BusinessException(ResultCode.TRANSPORT_NOT_FOUND, transportResult.getMessage());
        }
        log.info("运单创建成功: waybillId={}", transportResult.getData().getId());

        // 5. 更新订单关联运单ID
        order.setWaybillId(transportResult.getData().getId());
        this.updateById(order);

        log.info("订单创建完成: orderId={}, orderNo={}, waybillId={}",
                order.getId(), orderNo, order.getWaybillId());

        return convertToVO(order);
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return convertToVO(order);
    }

    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = this.getOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 只有待支付状态可以取消
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "当前订单状态不可取消");
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        boolean updated = this.updateById(order);

        if (updated) {
            // 回滚库存
            inventoryClient.rollbackStock(order.getProductId(), order.getProductCount());
            log.info("订单取消成功，库存已回滚: orderId={}", orderId);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markOrderAsPaid(String orderNo, BigDecimal paidAmount) {
        log.info("开始处理支付完成回调: orderNo={}, paidAmount={}", orderNo, paidAmount);

        // 1. 查询订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = this.getOne(wrapper);

        if (order == null) {
            log.error("订单不存在: orderNo={}", orderNo);
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 2. 检查订单状态（只有待支付状态才能更新为已支付）
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            log.error("订单状态错误: orderNo={}, status={}", orderNo, order.getStatus());
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不是待支付");
        }

        // 3. 验证支付金额
        if (paidAmount.compareTo(order.getAmount()) != 0) {
            log.error("支付金额不匹配: orderNo={}, expectedAmount={}, paidAmount={}",
                    orderNo, order.getAmount(), paidAmount);
            throw new BusinessException(ResultCode.FAIL, "支付金额不匹配");
        }

        // 4. 更新订单状态为已支付
        order.setStatus(OrderStatus.PAID.getCode());
        boolean updated = this.updateById(order);

        if (updated) {
            log.info("订单支付状态更新成功: orderId={}, orderNo={}, status=PAID", order.getId(), orderNo);
        } else {
            log.error("订单支付状态更新失败: orderId={}, orderNo={}", order.getId(), orderNo);
            throw new BusinessException(ResultCode.FAIL, "更新订单状态失败");
        }

        return true;
    }

    @Override
    public IPage<OrderVO> listByUserId(Long userId, Integer page, Integer pageSize) {
        log.info("查询用户订单列表: userId={}, page={}, pageSize={}", userId, page, pageSize);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        Page<Order> pageParam = new Page<>(page, pageSize);
        IPage<Order> orderPage = this.page(pageParam, wrapper);

        // 转换为 VO
        return orderPage.convert(this::convertToVO);
    }

    /**
     * 生成订单编号
     * 格式: OD + 时间戳 + 雪花ID后6位
     */
    private String generateOrderNo() {
        long id = snowflake.nextId();
        return "OD" + System.currentTimeMillis() + String.valueOf(id).substring(12);
    }

    /**
     * 转换为 VO
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);
        OrderStatus status = OrderStatus.of(order.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDesc());
        }
        return vo;
    }
}
