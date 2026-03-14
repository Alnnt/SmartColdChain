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
 * 杩愯緭鏈嶅姟瀹炵幇绫?
 *
 * @author Alnnt
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
        log.info("寮€濮嬪垱寤鸿繍鍗? orderId={}, fromAddress={}, toAddress={}", orderId, fromAddress, toAddress);

        // 1. 鏌ユ壘绗竴涓┖闂插徃鏈猴紙浣跨敤 SELECT ... FOR UPDATE 鍔犻攣锛?
        Driver freeDriver = driverMapper.findFirstFreeDriver();

        if (freeDriver == null) {
            log.error("没有可用的空闲司机");
            throw new RuntimeException("No available drivers");
        }

        log.info("鎵惧埌绌洪棽鍙告満: {} (ID={}), 杞︾墝: {}",
                freeDriver.getName(), freeDriver.getId(), freeDriver.getLicensePlate());

        // 2. 閿佸畾鍙告満锛堢姸鎬佹敼涓築USY锛?
        int updateCount = driverMapper.updateDriverStatus(
                freeDriver.getId(),
                DriverStatus.FREE.getCode(),
                DriverStatus.BUSY.getCode());

        if (updateCount == 0) {
            log.error("閿佸畾鍙告満澶辫触锛屽彲鑳藉瓨鍦ㄥ苟鍙戝啿绐? driverId={}", freeDriver.getId());
            throw new RuntimeException("Failed to lock driver, please retry");
        }

        log.info("鍙告満宸查攣瀹? {} (ID={})", freeDriver.getName(), freeDriver.getId());

        // 3. 鍒涘缓杩愬崟璁板綍
        TransportOrder transportOrder = TransportOrder.builder()
                .orderId(orderId)
                .driverId(freeDriver.getId())
                .startAddress(fromAddress)
                .endAddress(toAddress)
                .status(TransportStatus.WAITING.getCode())
                .build();

        transportOrderMapper.insert(transportOrder);

        log.info("杩愬崟鍒涘缓鎴愬姛, transportOrderId={}, driverId={}, orderId={}",
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
        log.info("寮€濮嬭繍杈? transportOrderId={}", transportOrderId);

        int updateCount = transportOrderMapper.updateStatus(
                transportOrderId,
                TransportStatus.WAITING.getCode(),
                TransportStatus.IN_TRANSIT.getCode());

        if (updateCount == 0) {
            log.error("鏇存柊杩愬崟鐘舵€佸け璐? transportOrderId={}", transportOrderId);
            return false;
        }

        log.info("杩愬崟鐘舵€佸凡鏇存柊涓鸿繍杈撲腑, transportOrderId={}", transportOrderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeTransport(Long transportOrderId) {
        log.info("瀹屾垚杩愯緭, transportOrderId={}", transportOrderId);

        // 1. 鑾峰彇杩愬崟淇℃伅
        TransportOrder transportOrder = transportOrderMapper.selectById(transportOrderId);
        if (transportOrder == null) {
            log.error("杩愬崟涓嶅瓨鍦? transportOrderId={}", transportOrderId);
            return false;
        }

        // 2. 鏇存柊杩愬崟鐘舵€佷负宸查€佽揪
        int updateCount = transportOrderMapper.markAsDelivered(transportOrderId);

        if (updateCount == 0) {
            log.error("鏇存柊杩愬崟鐘舵€佸け璐? transportOrderId={}", transportOrderId);
            return false;
        }

        // 3. 閲婃斁鍙告満锛堢姸鎬佹敼涓篎REE锛?
        int driverUpdateCount = driverMapper.updateDriverStatus(
                transportOrder.getDriverId(),
                DriverStatus.BUSY.getCode(),
                DriverStatus.FREE.getCode());

        if (driverUpdateCount == 0) {
            log.warn("閲婃斁鍙告満鐘舵€佸け璐? driverId={}", transportOrder.getDriverId());
            // 涓嶆姏寮傚父锛岃繍鍗曞凡瀹屾垚
        }

        log.info("杩愯緭瀹屾垚, transportOrderId={}, driverId={}",
                transportOrderId, transportOrder.getDriverId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTransport(Long transportOrderId) {
        log.info("鍙栨秷杩愬崟, transportOrderId={}", transportOrderId);

        // 1. 鑾峰彇杩愬崟淇℃伅
        TransportOrder transportOrder = transportOrderMapper.selectById(transportOrderId);
        if (transportOrder == null) {
            log.error("杩愬崟涓嶅瓨鍦? transportOrderId={}", transportOrderId);
            return false;
        }

        // 鍙湁寰呭彇璐х姸鎬佺殑杩愬崟鍙互鍙栨秷
        if (!TransportStatus.WAITING.getCode().equals(transportOrder.getStatus())) {
            log.error("杩愬崟鐘舵€佷笉鍏佽鍙栨秷, transportOrderId={}, status={}",
                    transportOrderId, transportOrder.getStatus());
            return false;
        }

        // 2. 鏇存柊杩愬崟鐘舵€佷负宸插彇娑?
        int updateCount = transportOrderMapper.updateStatus(
                transportOrderId,
                TransportStatus.WAITING.getCode(),
                TransportStatus.CANCELLED.getCode());

        if (updateCount == 0) {
            log.error("鏇存柊杩愬崟鐘舵€佸け璐? transportOrderId={}", transportOrderId);
            return false;
        }

        // 3. 閲婃斁鍙告満
        int driverUpdateCount = driverMapper.updateDriverStatus(
                transportOrder.getDriverId(),
                DriverStatus.BUSY.getCode(),
                DriverStatus.FREE.getCode());

        if (driverUpdateCount == 0) {
            log.warn("閲婃斁鍙告満鐘舵€佸け璐? driverId={}", transportOrder.getDriverId());
        }

        log.info("杩愬崟宸插彇娑? transportOrderId={}", transportOrderId);
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
