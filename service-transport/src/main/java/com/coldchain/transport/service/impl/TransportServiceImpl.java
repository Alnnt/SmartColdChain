package com.coldchain.transport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.transport.entity.Driver;
import com.coldchain.transport.entity.TransportOrder;
import com.coldchain.transport.enums.DriverStatus;
import com.coldchain.transport.enums.TransportStatus;
import com.coldchain.transport.mapper.DriverMapper;
import com.coldchain.transport.mapper.TransportOrderMapper;
import com.coldchain.transport.service.TransportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 运输服务实现类
 *
 * @author ColdChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransportServiceImpl implements TransportService {

    private final DriverMapper driverMapper;
    private final TransportOrderMapper transportOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportOrder createWaybill(Long orderId, String fromAddress, String toAddress) {
        log.info("开始创建运单, orderId={}, fromAddress={}, toAddress={}", orderId, fromAddress, toAddress);

        // 1. 查找第一个空闲司机（使用 SELECT ... FOR UPDATE 加锁）
        Driver freeDriver = driverMapper.findFirstFreeDriver();

        if (freeDriver == null) {
            log.error("没有可用的空闲司机");
            throw new RuntimeException("No available drivers");
        }

        log.info("找到空闲司机: {} (ID={}), 车牌: {}",
                freeDriver.getName(), freeDriver.getId(), freeDriver.getLicensePlate());

        // 2. 锁定司机（状态改为BUSY）
        int updateCount = driverMapper.updateDriverStatus(
                freeDriver.getId(),
                DriverStatus.FREE.getCode(),
                DriverStatus.BUSY.getCode()
        );

        if (updateCount == 0) {
            log.error("锁定司机失败，可能存在并发冲突, driverId={}", freeDriver.getId());
            throw new RuntimeException("Failed to lock driver, please retry");
        }

        log.info("司机已锁定: {} (ID={})", freeDriver.getName(), freeDriver.getId());

        // 3. 创建运单记录
        TransportOrder transportOrder = TransportOrder.builder()
                .orderId(orderId)
                .driverId(freeDriver.getId())
                .startAddress(fromAddress)
                .endAddress(toAddress)
                .status(TransportStatus.WAITING.getCode())
                .build();

        transportOrderMapper.insert(transportOrder);

        log.info("运单创建成功, transportOrderId={}, driverId={}, orderId={}",
                transportOrder.getId(), freeDriver.getId(), orderId);

        return transportOrder;
    }

    @Override
    public TransportOrder getByOrderId(Long orderId) {
        return transportOrderMapper.findByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startTransport(Long transportOrderId) {
        log.info("开始运输, transportOrderId={}", transportOrderId);

        int updateCount = transportOrderMapper.updateStatus(
                transportOrderId,
                TransportStatus.WAITING.getCode(),
                TransportStatus.IN_TRANSIT.getCode()
        );

        if (updateCount == 0) {
            log.error("更新运单状态失败, transportOrderId={}", transportOrderId);
            return false;
        }

        log.info("运单状态已更新为运输中, transportOrderId={}", transportOrderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeTransport(Long transportOrderId) {
        log.info("完成运输, transportOrderId={}", transportOrderId);

        // 1. 获取运单信息
        TransportOrder transportOrder = transportOrderMapper.selectById(transportOrderId);
        if (transportOrder == null) {
            log.error("运单不存在, transportOrderId={}", transportOrderId);
            return false;
        }

        // 2. 更新运单状态为已送达
        int updateCount = transportOrderMapper.markAsDelivered(transportOrderId);

        if (updateCount == 0) {
            log.error("更新运单状态失败, transportOrderId={}", transportOrderId);
            return false;
        }

        // 3. 释放司机（状态改为FREE）
        int driverUpdateCount = driverMapper.updateDriverStatus(
                transportOrder.getDriverId(),
                DriverStatus.BUSY.getCode(),
                DriverStatus.FREE.getCode()
        );

        if (driverUpdateCount == 0) {
            log.warn("释放司机状态失败, driverId={}", transportOrder.getDriverId());
            // 不抛异常，运单已完成
        }

        log.info("运输完成, transportOrderId={}, driverId={}",
                transportOrderId, transportOrder.getDriverId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTransport(Long transportOrderId) {
        log.info("取消运单, transportOrderId={}", transportOrderId);

        // 1. 获取运单信息
        TransportOrder transportOrder = transportOrderMapper.selectById(transportOrderId);
        if (transportOrder == null) {
            log.error("运单不存在, transportOrderId={}", transportOrderId);
            return false;
        }

        // 只有待取货状态的运单可以取消
        if (!TransportStatus.WAITING.getCode().equals(transportOrder.getStatus())) {
            log.error("运单状态不允许取消, transportOrderId={}, status={}",
                    transportOrderId, transportOrder.getStatus());
            return false;
        }

        // 2. 更新运单状态为已取消
        int updateCount = transportOrderMapper.updateStatus(
                transportOrderId,
                TransportStatus.WAITING.getCode(),
                TransportStatus.CANCELLED.getCode()
        );

        if (updateCount == 0) {
            log.error("更新运单状态失败, transportOrderId={}", transportOrderId);
            return false;
        }

        // 3. 释放司机
        int driverUpdateCount = driverMapper.updateDriverStatus(
                transportOrder.getDriverId(),
                DriverStatus.BUSY.getCode(),
                DriverStatus.FREE.getCode()
        );

        if (driverUpdateCount == 0) {
            log.warn("释放司机状态失败, driverId={}", transportOrder.getDriverId());
        }

        log.info("运单已取消, transportOrderId={}", transportOrderId);
        return true;
    }

    @Override
    public List<TransportOrder> getActiveTransportsByDriver(Long driverId) {
        return transportOrderMapper.findActiveByDriverId(driverId);
    }

    @Override
    public List<Driver> getAvailableDrivers() {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getStatus, DriverStatus.FREE.getCode())
                .eq(Driver::getDeleted, 0);
        return driverMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDriverLocation(Long driverId, Double latitude, Double longitude) {
        int updateCount = driverMapper.updateDriverLocation(driverId, latitude, longitude);
        return updateCount > 0;
    }
}
