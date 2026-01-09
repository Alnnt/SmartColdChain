package com.coldchain.transport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.transport.entity.Driver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 司机Mapper接口
 *
 * @author ColdChain
 */
@Mapper
public interface DriverMapper extends BaseMapper<Driver> {

    /**
     * 查找第一个空闲的司机
     *
     * @return 空闲司机，如果没有则返回null
     */
    @Select("""
            SELECT id, name, phone, license_plate, status, latitude, longitude,
                   create_time, update_time, deleted
            FROM t_driver
            WHERE status = 0
              AND deleted = 0
            LIMIT 1
            FOR UPDATE
            """)
    Driver findFirstFreeDriver();

    /**
     * 更新司机状态（使用行锁保证并发安全）
     *
     * @param driverId  司机ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @return 受影响的行数
     */
    @Update("""
            UPDATE t_driver
            SET status = #{newStatus},
                update_time = NOW()
            WHERE id = #{driverId}
              AND status = #{oldStatus}
              AND deleted = 0
            """)
    int updateDriverStatus(@Param("driverId") Long driverId,
                           @Param("oldStatus") Integer oldStatus,
                           @Param("newStatus") Integer newStatus);

    /**
     * 更新司机位置
     *
     * @param driverId  司机ID
     * @param latitude  纬度
     * @param longitude 经度
     * @return 受影响的行数
     */
    @Update("""
            UPDATE t_driver
            SET latitude = #{latitude},
                longitude = #{longitude},
                update_time = NOW()
            WHERE id = #{driverId}
              AND deleted = 0
            """)
    int updateDriverLocation(@Param("driverId") Long driverId,
                             @Param("latitude") Double latitude,
                             @Param("longitude") Double longitude);
}
