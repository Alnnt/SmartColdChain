package com.coldchain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 *
 * @author Alnnt
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
