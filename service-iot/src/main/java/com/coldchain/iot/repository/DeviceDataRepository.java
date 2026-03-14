package com.coldchain.iot.repository;

import com.coldchain.iot.model.DeviceData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备数据 MongoDB Repository
 *
 * @author Alnnt
 */
@Repository
public interface DeviceDataRepository extends MongoRepository<DeviceData, String> {

    /**
     * 根据设备ID查询最新数据
     */
    DeviceData findTopByDeviceIdOrderByTimestampDesc(String deviceId);

    /**
     * 根据设备ID和时间范围查询
     */
    List<DeviceData> findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
            String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据设备ID分页查询
     */
    Page<DeviceData> findByDeviceIdOrderByTimestampDesc(String deviceId, Pageable pageable);

    /**
     * 查询报警数据
     */
    List<DeviceData> findByAlarmStatusNotAndTimestampAfter(Integer normalStatus, LocalDateTime afterTime);

    /**
     * 根据运单ID查询轨迹数据
     */
    List<DeviceData> findByWaybillIdOrderByTimestampAsc(Long waybillId);

    /**
     * 统计设备在时间范围内的数据条数
     */
    long countByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询温度异常的数据
     */
    @Query("{ 'deviceId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 }, $or: [ { 'temperature': { $lt: ?3 } }, { 'temperature': { $gt: ?4 } } ] }")
    List<DeviceData> findTemperatureAbnormal(String deviceId, LocalDateTime startTime, LocalDateTime endTime,
                                              Double minTemp, Double maxTemp);
}
