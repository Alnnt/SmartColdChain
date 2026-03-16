package com.coldchain.order.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coldchain.common.exception.BusinessException;
import com.coldchain.common.result.Result;
import com.coldchain.common.result.ResultCode;
import com.coldchain.order.dto.OrderCreateDTO;
import com.coldchain.order.dto.OrderCreateItemDTO;
import com.coldchain.order.dto.OrderItemVO;
import com.coldchain.order.dto.OrderVO;
import com.coldchain.order.entity.Order;
import com.coldchain.order.entity.OrderItem;
import com.coldchain.order.entity.enums.OrderStatus;
import com.coldchain.order.feign.InventoryClient;
import com.coldchain.order.feign.TransportClient;
import com.coldchain.order.feign.dto.DecreaseStockVO;
import com.coldchain.order.feign.dto.WaybillCreateDTO;
import com.coldchain.order.feign.dto.WaybillVO;
import com.coldchain.order.mapper.OrderItemMapper;
import com.coldchain.order.mapper.OrderMapper;
import com.coldchain.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现（一个订单多个商品项）
 *
 * @author ColdChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final InventoryClient inventoryClient;
    private final TransportClient transportClient;

    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateDTO dto, Long userId) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单商品不能为空");
        }
        Long addressId = parseId(dto.getAddressId(), "收货地址ID");
        log.info("开始创建订单: userId={}, items={}", userId, dto.getItems().size());

        String orderNo = generateOrderNo();
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .amount(dto.getAmount())
                .status(OrderStatus.PENDING_PAYMENT.getCode())
                .addressId(addressId)
                .build();

        boolean saved = this.save(order);
        if (!saved) {
            throw new BusinessException(ResultCode.FAIL, "订单创建失败");
        }
        log.info("订单创建成功: orderId={}, orderNo={}", order.getId(), orderNo);

        Long firstWarehouseId = null;
        for (OrderCreateItemDTO itemDto : dto.getItems()) {
            Long productId = parseId(itemDto.getProductId(), "商品ID");
            Result<DecreaseStockVO> inventoryResult = inventoryClient.freezeStock(
                    String.valueOf(productId), itemDto.getProductCount());
            if (!inventoryResult.isSuccess()) {
                log.error("库存冻结失败: productId={}, {}", productId, inventoryResult.getMessage());
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH, inventoryResult.getMessage());
            }
            DecreaseStockVO freezeData = inventoryResult.getData();
            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(productId)
                    .productName(null)
                    .count(itemDto.getProductCount())
                    .amount(itemDto.getAmount())
                    .inventoryId(freezeData != null ? freezeData.getInventoryId() : null)
                    .warehouseId(freezeData != null ? freezeData.getWarehouseId() : null)
                    .build();
            orderItemMapper.insert(item);
            if (firstWarehouseId == null && freezeData != null && freezeData.getWarehouseId() != null) {
                firstWarehouseId = freezeData.getWarehouseId();
            }
        }

        if (firstWarehouseId != null) {
            order.setWarehouseId(firstWarehouseId);
            this.updateById(order);
        }

        return convertToVO(order, getItemsByOrderId(order.getId()));
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return convertToVO(order, getItemsByOrderId(orderId));
    }

    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = this.getOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return convertToVO(order, getItemsByOrderId(order.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "当前订单状态不可取消");
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        boolean updated = this.updateById(order);
        if (updated) {
            List<OrderItem> items = listItemsByOrderId(orderId);
            for (OrderItem item : items) {
                if (item.getInventoryId() != null) {
                    Result<Boolean> cancelResult = inventoryClient.cancelFreeze(
                            String.valueOf(item.getInventoryId()), item.getCount());
                    if (cancelResult.isSuccess()) {
                        log.info("释放冻结库存: orderItemId={}", item.getId());
                    } else {
                        log.warn("释放冻结库存失败: orderItemId={}, msg={}", item.getId(), cancelResult.getMessage());
                    }
                }
            }
        }
        return updated;
    }

    @Override
    @GlobalTransactional(name = "pay-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean markOrderAsPaid(String orderNo, BigDecimal paidAmount) {
        log.info("处理支付: orderNo={}, paidAmount={}", orderNo, paidAmount);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = this.getOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不是待支付");
        }
        if (paidAmount.compareTo(order.getAmount()) != 0) {
            throw new BusinessException(ResultCode.FAIL, "支付金额不匹配");
        }

        List<OrderItem> items = listItemsByOrderId(order.getId());
        for (OrderItem item : items) {
            if (item.getInventoryId() == null) {
                throw new BusinessException(ResultCode.FAIL, "订单明细未关联库存，无法完成支付");
            }
            Result<Boolean> confirmResult = inventoryClient.confirmDeduct(
                    String.valueOf(item.getInventoryId()), item.getCount());
            if (!confirmResult.isSuccess()) {
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH, confirmResult.getMessage());
            }
        }

        WaybillCreateDTO waybillDTO = WaybillCreateDTO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .addressId(order.getAddressId())
                .build();
        Result<WaybillVO> transportResult = transportClient.createWaybill(waybillDTO);
        if (!transportResult.isSuccess()) {
            throw new BusinessException(ResultCode.TRANSPORT_NOT_FOUND, transportResult.getMessage());
        }
        WaybillVO waybillVO = transportResult.getData();
        if (waybillVO == null || waybillVO.getId() == null) {
            throw new BusinessException(ResultCode.TRANSPORT_NOT_FOUND, "运单返回数据异常");
        }

        order.setWaybillId(waybillVO.getId());
        order.setStatus(OrderStatus.PAID.getCode());
        boolean updated = this.updateById(order);
        if (!updated) {
            throw new BusinessException(ResultCode.FAIL, "更新订单状态失败");
        }
        log.info("订单支付完成: orderId={}, orderNo={}", order.getId(), orderNo);
        return true;
    }

    @Override
    public IPage<OrderVO> listByWarehouseId(Long warehouseId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime);
        if (warehouseId != null) {
            wrapper.eq(Order::getWarehouseId, warehouseId);
        }
        Page<Order> pageParam = new Page<>(page, pageSize);
        IPage<Order> orderPage = this.page(pageParam, wrapper);
        return orderPage.convert(o -> convertToVO(o, getItemsByOrderId(o.getId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean shipOrder(Long orderId, Long warehouseId, boolean isSuperAdmin) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!OrderStatus.PAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有已支付订单可发货");
        }
        if (!isSuperAdmin && warehouseId != null && !warehouseId.equals(order.getWarehouseId())) {
            throw new BusinessException(ResultCode.FAIL, "无权操作该仓库的订单");
        }
        order.setStatus(OrderStatus.SHIPPED.getCode());
        return this.updateById(order);
    }

    @Override
    public IPage<OrderVO> listByUserId(Long userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime);
        Page<Order> pageParam = new Page<>(page, pageSize);
        IPage<Order> orderPage = this.page(pageParam, wrapper);
        return orderPage.convert(o -> convertToVO(o, getItemsByOrderId(o.getId())));
    }

    private List<OrderItem> listItemsByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderItem> q = new LambdaQueryWrapper<>();
        q.eq(OrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(q);
    }

    private List<OrderItemVO> getItemsByOrderId(Long orderId) {
        List<OrderItem> list = listItemsByOrderId(orderId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toItemVO).collect(Collectors.toList());
    }

    private OrderItemVO toItemVO(OrderItem item) {
        return OrderItemVO.builder()
                .id(item.getId() != null ? String.valueOf(item.getId()) : null)
                .productId(item.getProductId() != null ? String.valueOf(item.getProductId()) : null)
                .productName(item.getProductName())
                .count(item.getCount())
                .amount(item.getAmount())
                .build();
    }

    private String generateOrderNo() {
        long id = snowflake.nextId();
        return "OD" + System.currentTimeMillis() + String.valueOf(id).substring(12);
    }

    private static Long parseId(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, name + "不能为空");
        }
        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, name + "格式无效");
        }
    }

    private OrderVO convertToVO(Order order, List<OrderItemVO> items) {
        OrderVO vo = OrderVO.builder()
                .id(order.getId() != null ? String.valueOf(order.getId()) : null)
                .orderNo(order.getOrderNo())
                .userId(order.getUserId() != null ? String.valueOf(order.getUserId()) : null)
                .amount(order.getAmount())
                .status(order.getStatus())
                .warehouseId(order.getWarehouseId() != null ? String.valueOf(order.getWarehouseId()) : null)
                .addressId(order.getAddressId() != null ? String.valueOf(order.getAddressId()) : null)
                .waybillId(order.getWaybillId() != null ? String.valueOf(order.getWaybillId()) : null)
                .createTime(order.getCreateTime())
                .items(items != null ? items : Collections.emptyList())
                .build();
        OrderStatus status = OrderStatus.of(order.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDesc());
        }
        return vo;
    }
}
