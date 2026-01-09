package com.coldchain.inventory.service.impl;

import com.coldchain.inventory.dto.DeductStockResponse;
import com.coldchain.inventory.dto.WarehouseStockDTO;
import com.coldchain.inventory.mapper.InventoryMapper;
import com.coldchain.inventory.service.InventoryService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 库存服务实现类
 *
 * @author ColdChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;

    /**
     * 地球半径（公里）
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    @GlobalTransactional(name = "deduct-stock-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public DeductStockResponse deductStock(Long productId, Integer count, Double userLat, Double userLon) {
        log.info("开始智能库存扣减, productId={}, count={}, userLocation=({}, {})",
                productId, count, userLat, userLon);

        // 1. 查询所有库存充足的仓库
        List<WarehouseStockDTO> warehouses = inventoryMapper.findWarehousesWithStock(productId, count);

        if (warehouses == null || warehouses.isEmpty()) {
            log.warn("没有找到库存充足的仓库, productId={}, count={}", productId, count);
            return DeductStockResponse.builder()
                    .success(false)
                    .build();
        }

        log.info("找到 {} 个库存充足的仓库", warehouses.size());

        // 2. 智能调度：找到距离用户最近的仓库
        WarehouseStockDTO nearestWarehouse = findNearestWarehouse(warehouses, userLat, userLon);

        if (nearestWarehouse == null) {
            log.error("无法计算最近仓库");
            throw new RuntimeException("无法计算最近仓库");
        }

        double distance = calculateDistance(
                userLat, userLon,
                nearestWarehouse.getLatitude(), nearestWarehouse.getLongitude()
        );

        log.info("选中最近仓库: {} (ID={}), 距离: {:.2f} km",
                nearestWarehouse.getWarehouseName(),
                nearestWarehouse.getWarehouseId(),
                distance);

        // 3. 乐观锁扣减库存
        int affectedRows = inventoryMapper.deductStockWithOptimisticLock(
                nearestWarehouse.getInventoryId(), count);

        if (affectedRows == 0) {
            log.error("库存扣减失败，可能存在并发冲突, inventoryId={}", nearestWarehouse.getInventoryId());
            // 抛出异常触发 Seata 全局事务回滚
            throw new RuntimeException("库存扣减失败：库存不足或并发冲突，请重试");
        }

        log.info("库存扣减成功, inventoryId={}, deductedCount={}",
                nearestWarehouse.getInventoryId(), count);

        return DeductStockResponse.builder()
                .success(true)
                .warehouseId(nearestWarehouse.getWarehouseId())
                .warehouseName(nearestWarehouse.getWarehouseName())
                .distance(Math.round(distance * 100.0) / 100.0) // 保留两位小数
                .remainingStock(nearestWarehouse.getAvailableStock() - count)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeductStockResponse freezeStock(Long productId, Integer count, Double userLat, Double userLon) {
        log.info("开始冻结库存 (TCC-Try), productId={}, count={}", productId, count);

        // 1. 查询所有库存充足的仓库
        List<WarehouseStockDTO> warehouses = inventoryMapper.findWarehousesWithStock(productId, count);

        if (warehouses == null || warehouses.isEmpty()) {
            log.warn("没有找到库存充足的仓库, productId={}, count={}", productId, count);
            return DeductStockResponse.builder()
                    .success(false)
                    .build();
        }

        // 2. 找到最近的仓库
        WarehouseStockDTO nearestWarehouse = findNearestWarehouse(warehouses, userLat, userLon);

        if (nearestWarehouse == null) {
            throw new RuntimeException("无法计算最近仓库");
        }

        double distance = calculateDistance(
                userLat, userLon,
                nearestWarehouse.getLatitude(), nearestWarehouse.getLongitude()
        );

        // 3. 冻结库存
        int affectedRows = inventoryMapper.freezeStock(nearestWarehouse.getInventoryId(), count);

        if (affectedRows == 0) {
            log.error("库存冻结失败, inventoryId={}", nearestWarehouse.getInventoryId());
            throw new RuntimeException("库存冻结失败：库存不足或并发冲突");
        }

        log.info("库存冻结成功, inventoryId={}, frozenCount={}",
                nearestWarehouse.getInventoryId(), count);

        return DeductStockResponse.builder()
                .success(true)
                .warehouseId(nearestWarehouse.getWarehouseId())
                .warehouseName(nearestWarehouse.getWarehouseName())
                .distance(Math.round(distance * 100.0) / 100.0)
                .remainingStock(nearestWarehouse.getAvailableStock() - count)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDeduct(Long inventoryId, Integer count) {
        log.info("确认扣减库存 (TCC-Confirm), inventoryId={}, count={}", inventoryId, count);

        int affectedRows = inventoryMapper.confirmDeductStock(inventoryId, count);

        if (affectedRows == 0) {
            log.error("确认扣减失败, inventoryId={}", inventoryId);
            return false;
        }

        log.info("确认扣减成功, inventoryId={}", inventoryId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelFreeze(Long inventoryId, Integer count) {
        log.info("取消冻结库存 (TCC-Cancel), inventoryId={}, count={}", inventoryId, count);

        int affectedRows = inventoryMapper.unfreezeStock(inventoryId, count);

        if (affectedRows == 0) {
            log.error("取消冻结失败, inventoryId={}", inventoryId);
            return false;
        }

        log.info("取消冻结成功, inventoryId={}", inventoryId);
        return true;
    }

    /**
     * 找到距离用户最近的仓库
     *
     * @param warehouses 仓库列表
     * @param userLat    用户纬度
     * @param userLon    用户经度
     * @return 最近的仓库
     */
    private WarehouseStockDTO findNearestWarehouse(List<WarehouseStockDTO> warehouses,
                                                    Double userLat, Double userLon) {
        return warehouses.stream()
                .min(Comparator.comparingDouble(warehouse ->
                        calculateDistance(userLat, userLon,
                                warehouse.getLatitude(), warehouse.getLongitude())))
                .orElse(null);
    }

    /**
     * 使用 Haversine 公式计算两点之间的球面距离
     * <p>
     * Haversine 公式：
     * a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
     * c = 2 * atan2(√a, √(1-a))
     * d = R * c
     *
     * @param lat1 点1纬度（度）
     * @param lon1 点1经度（度）
     * @param lat2 点2纬度（度）
     * @param lon2 点2经度（度）
     * @return 距离（公里）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Haversine 公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离
        return EARTH_RADIUS_KM * c;
    }
}
