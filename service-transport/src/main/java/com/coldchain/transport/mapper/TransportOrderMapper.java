package com.coldchain.transport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.transport.entity.TransportOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 运单Mapper接口
 *
 * @author ColdChain
 */
@Mapper
public interface TransportOrderMapper extends BaseMapper<TransportOrder> {

    /**
     * 根据订单ID查询运单
     *
     * @param orderId 订单ID
     * @return 运单
     */
    @Select("""
            SELECT id, order_id, driver_id, start_address, end_address, status,
                   estimated_arrival_time, actual_arrival_time, remark,
                   create_time, update_time, deleted
            FROM t_transport_order
            WHERE order_id = #{orderId}
              AND deleted = 0
            """)
    TransportOrder findByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据司机ID查询进行中的运单
     *
     * @param driverId 司机ID
     * @return 运单列表
     */
    @Select("""
            SELECT id, order_id, driver_id, start_address, end_address, status,
                   estimated_arrival_time, actual_arrival_time, remark,
                   create_time, update_time, deleted
            FROM t_transport_order
            WHERE driver_id = #{driverId}
              AND status IN (0, 1)
              AND deleted = 0
            ORDER BY create_time DESC
            """)
    List<TransportOrder> findActiveByDriverId(@Param("driverId") Long driverId);

    /**
     * 更新运单状态
     *
     * @param transportOrderId 运单ID
     * @param oldStatus        原状态
     * @param newStatus        新状态
     * @return 受影响的行数
     */
    @Update("""
            UPDATE t_transport_order
            SET status = #{newStatus},
                update_time = NOW()
            WHERE id = #{transportOrderId}
              AND status = #{oldStatus}
              AND deleted = 0
            """)
    int updateStatus(@Param("transportOrderId") Long transportOrderId,
                     @Param("oldStatus") Integer oldStatus,
                     @Param("newStatus") Integer newStatus);

    /**
     * 更新运单状态并设置实际到达时间
     *
     * @param transportOrderId 运单ID
     * @return 受影响的行数
     */
    @Update("""
            UPDATE t_transport_order
            SET status = 2,
                actual_arrival_time = NOW(),
                update_time = NOW()
            WHERE id = #{transportOrderId}
              AND status = 1
              AND deleted = 0
            """)
    int markAsDelivered(@Param("transportOrderId") Long transportOrderId);
}
