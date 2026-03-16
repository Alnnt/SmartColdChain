package com.coldchain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper
 *
 * @author ColdChain
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
