package com.coldchain.transport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.transport.entity.TransportOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 杩愬崟Mapper鎺ュ彛
 *
 * @author Alnnt
 */
@Mapper
public interface TransportOrderMapper extends BaseMapper<TransportOrder> {

  /**
   * 鏍规嵁璁㈠崟ID鏌ヨ杩愬崟
   *
   * @param orderId 璁㈠崟ID
   * @return 杩愬崟
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
   * 鏍规嵁鍙告満ID鏌ヨ杩涜涓殑杩愬崟
   *
   * @param driverId 鍙告満ID
   * @return 杩愬崟鍒楄〃
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
   * 鏇存柊杩愬崟鐘舵€?
   *
   * @param transportOrderId 杩愬崟ID
   * @param oldStatus        鍘熺姸鎬?
   * @param newStatus        鏂扮姸鎬?
   * @return 鍙楀奖鍝嶇殑琛屾暟
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
   * 鏇存柊杩愬崟鐘舵€佸苟璁剧疆瀹為檯鍒拌揪鏃堕棿
   *
   * @param transportOrderId 杩愬崟ID
   * @return 鍙楀奖鍝嶇殑琛屾暟
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
