package com.coldchain.inventory.service.impl;

import com.coldchain.inventory.dto.DeductStockResponse;
import com.coldchain.inventory.dto.InventoryItemDTO;
import com.coldchain.inventory.dto.WarehouseStockDTO;
import com.coldchain.inventory.entity.Inventory;
import com.coldchain.inventory.entity.Warehouse;
import com.coldchain.inventory.mapper.InventoryMapper;
import com.coldchain.inventory.mapper.WarehouseMapper;
import com.coldchain.inventory.service.InventoryService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 搴撳瓨鏈嶅姟瀹炵幇绫?
 *
 * @author Alnnt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;

    /**
     * 鍦扮悆鍗婂緞锛堝叕閲岋級
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    @GlobalTransactional(name = "deduct-stock-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public DeductStockResponse deductStock(Long productId, Integer count, Double userLat, Double userLon) {
        log.info("寮€濮嬫櫤鑳藉簱瀛樻墸鍑? productId={}, count={}, userLocation=({}, {})",
                productId, count, userLat, userLon);

        // 1. 鏌ヨ鎵€鏈夊簱瀛樺厖瓒崇殑浠撳簱
        List<WarehouseStockDTO> warehouses = inventoryMapper.findWarehousesWithStock(productId, count);

        if (warehouses == null || warehouses.isEmpty()) {
            log.warn("娌℃湁鎵惧埌搴撳瓨鍏呰冻鐨勪粨搴? productId={}, count={}", productId, count);
            return DeductStockResponse.builder()
                    .success(false)
                    .build();
        }

        log.info("鎵惧埌 {} 涓簱瀛樺厖瓒崇殑浠撳簱", warehouses.size());

        // 2. 鏅鸿兘璋冨害锛氭壘鍒拌窛绂荤敤鎴锋渶杩戠殑浠撳簱
        WarehouseStockDTO nearestWarehouse = findNearestWarehouse(warehouses, userLat, userLon);

        if (nearestWarehouse == null) {
            log.error("无法计算最近仓库");
            throw new RuntimeException("无法计算最近仓库");
        }

        double distance = calculateDistance(
                userLat, userLon,
                nearestWarehouse.getLatitude(), nearestWarehouse.getLongitude());

        log.info("閫変腑鏈€杩戜粨搴? {} (ID={}), 璺濈: {:.2f} km",
                nearestWarehouse.getWarehouseName(),
                nearestWarehouse.getWarehouseId(),
                distance);

        // 3. 涔愯閿佹墸鍑忓簱瀛?
        int affectedRows = inventoryMapper.deductStockWithOptimisticLock(
                nearestWarehouse.getInventoryId(), count);

        if (affectedRows == 0) {
            log.error("搴撳瓨鎵ｅ噺澶辫触锛屽彲鑳藉瓨鍦ㄥ苟鍙戝啿绐? inventoryId={}", nearestWarehouse.getInventoryId());
            // 鎶涘嚭寮傚父瑙﹀彂 Seata 鍏ㄥ眬浜嬪姟鍥炴粴
            throw new RuntimeException("搴撳瓨鎵ｅ噺澶辫触锛氬簱瀛樹笉瓒虫垨骞跺彂鍐茬獊锛岃閲嶈瘯");
        }

        log.info("搴撳瓨鎵ｅ噺鎴愬姛, inventoryId={}, deductedCount={}",
                nearestWarehouse.getInventoryId(), count);

        return DeductStockResponse.builder()
                .success(true)
                .warehouseId(nearestWarehouse.getWarehouseId())
                .warehouseName(nearestWarehouse.getWarehouseName())
                .distance(Math.round(distance * 100.0) / 100.0) // 淇濈暀涓や綅灏忔暟
                .remainingStock(nearestWarehouse.getAvailableStock() - count)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeductStockResponse freezeStock(Long productId, Integer count, Double userLat, Double userLon) {
        log.info("寮€濮嬪喕缁撳簱瀛?(TCC-Try), productId={}, count={}", productId, count);

        // 1. 鏌ヨ鎵€鏈夊簱瀛樺厖瓒崇殑浠撳簱
        List<WarehouseStockDTO> warehouses = inventoryMapper.findWarehousesWithStock(productId, count);

        if (warehouses == null || warehouses.isEmpty()) {
            log.warn("娌℃湁鎵惧埌搴撳瓨鍏呰冻鐨勪粨搴? productId={}, count={}", productId, count);
            return DeductStockResponse.builder()
                    .success(false)
                    .build();
        }

        // 2. 鎵惧埌鏈€杩戠殑浠撳簱
        WarehouseStockDTO nearestWarehouse = findNearestWarehouse(warehouses, userLat, userLon);

        if (nearestWarehouse == null) {
            throw new RuntimeException("无法计算最近仓库");
        }

        double distance = calculateDistance(
                userLat, userLon,
                nearestWarehouse.getLatitude(), nearestWarehouse.getLongitude());

        // 3. 鍐荤粨搴撳瓨
        int affectedRows = inventoryMapper.freezeStock(nearestWarehouse.getInventoryId(), count);

        if (affectedRows == 0) {
            log.error("搴撳瓨鍐荤粨澶辫触, inventoryId={}", nearestWarehouse.getInventoryId());
            throw new RuntimeException("搴撳瓨鍐荤粨澶辫触锛氬簱瀛樹笉瓒虫垨骞跺彂鍐茬獊");
        }

        log.info("搴撳瓨鍐荤粨鎴愬姛, inventoryId={}, frozenCount={}",
                nearestWarehouse.getInventoryId(), count);

        return DeductStockResponse.builder()
                .success(true)
                .inventoryId(nearestWarehouse.getInventoryId())
                .warehouseId(nearestWarehouse.getWarehouseId())
                .warehouseName(nearestWarehouse.getWarehouseName())
                .distance(Math.round(distance * 100.0) / 100.0)
                .remainingStock(nearestWarehouse.getAvailableStock() - count)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDeduct(Long inventoryId, Integer count) {
        log.info("纭鎵ｅ噺搴撳瓨 (TCC-Confirm), inventoryId={}, count={}", inventoryId, count);

        int affectedRows = inventoryMapper.confirmDeductStock(inventoryId, count);

        if (affectedRows == 0) {
            log.error("纭鎵ｅ噺澶辫触, inventoryId={}", inventoryId);
            return false;
        }

        log.info("纭鎵ｅ噺鎴愬姛, inventoryId={}", inventoryId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelFreeze(Long inventoryId, Integer count) {
        log.info("鍙栨秷鍐荤粨搴撳瓨 (TCC-Cancel), inventoryId={}, count={}", inventoryId, count);

        int affectedRows = inventoryMapper.unfreezeStock(inventoryId, count);

        if (affectedRows == 0) {
            log.error("鍙栨秷鍐荤粨澶辫触, inventoryId={}", inventoryId);
            return false;
        }

        log.info("鍙栨秷鍐荤粨鎴愬姛, inventoryId={}", inventoryId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackStock(Long productId, Integer count) {
        log.info("回滚库存: productId={}, count={}", productId, count);
        int affected = inventoryMapper.rollbackStockByProductId(productId, count);
        return affected > 0;
    }

    @Override
    public List<Warehouse> listWarehouses(Long warehouseId) {
        if (warehouseId != null) {
            Warehouse one = warehouseMapper.selectById(warehouseId);
            return one != null ? List.of(one) : Collections.emptyList();
        }
        List<Warehouse> list = warehouseMapper.selectList(null);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public List<InventoryItemDTO> listInventoryItems(Long warehouseId) {
        List<InventoryItemDTO> list = warehouseId != null
                ? inventoryMapper.listInventoryItemsByWarehouseId(warehouseId)
                : inventoryMapper.listInventoryItems();
        return list != null ? list : Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addInventoryItem(Long warehouseId, Long productId, Integer quantity) {
        if (warehouseId == null || productId == null || quantity == null || quantity < 1) {
            return false;
        }
        LambdaQueryWrapper<Inventory> q = new LambdaQueryWrapper<>();
        q.eq(Inventory::getWarehouseId, warehouseId).eq(Inventory::getProductId, productId);
        Inventory existing = inventoryMapper.selectOne(q);
        if (existing != null) {
            return adjustStock(existing.getId(), quantity);
        }
        Inventory newInv = Inventory.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .totalStock(quantity)
                .frozenStock(0)
                .build();
        return inventoryMapper.insert(newInv) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustStock(Long inventoryId, Integer delta) {
        if (delta == null || delta == 0) {
            return false;
        }
        int affected = inventoryMapper.adjustStock(inventoryId, delta);
        if (affected > 0) {
            log.info("库存调整成功: inventoryId={}, delta={}", inventoryId, delta);
            return true;
        }
        log.warn("库存调整失败（可能违反约束）: inventoryId={}, delta={}", inventoryId, delta);
        return false;
    }

    /**
     * 鎵惧埌璺濈鐢ㄦ埛鏈€杩戠殑浠撳簱
     *
     * @param warehouses 浠撳簱鍒楄〃
     * @param userLat    鐢ㄦ埛绾害
     * @param userLon    鐢ㄦ埛缁忓害
     * @return 鏈€杩戠殑浠撳簱
     */
    private WarehouseStockDTO findNearestWarehouse(List<WarehouseStockDTO> warehouses,
            Double userLat, Double userLon) {
        return warehouses.stream()
                .min(Comparator.comparingDouble(warehouse -> calculateDistance(userLat, userLon,
                        warehouse.getLatitude(), warehouse.getLongitude())))
                .orElse(null);
    }

    /**
     * 浣跨敤 Haversine 鍏紡璁＄畻涓ょ偣涔嬮棿鐨勭悆闈㈣窛绂?
     * <p>
     * Haversine 鍏紡锛?
     * a = sin虏(螖lat/2) + cos(lat1) * cos(lat2) * sin虏(螖lon/2)
     * c = 2 * atan2(鈭歛, 鈭?1-a))
     * d = R * c
     *
     * @param lat1 鐐?绾害锛堝害锛?
     * @param lon1 鐐?缁忓害锛堝害锛?
     * @param lat2 鐐?绾害锛堝害锛?
     * @param lon2 鐐?缁忓害锛堝害锛?
     * @return 璺濈锛堝叕閲岋級
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 灏嗚搴﹁浆鎹负寮у害
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Haversine 鍏紡
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                        * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 璁＄畻璺濈
        return EARTH_RADIUS_KM * c;
    }
}
