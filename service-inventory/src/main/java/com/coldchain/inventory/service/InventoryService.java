package com.coldchain.inventory.service;

import com.coldchain.inventory.dto.DeductStockResponse;

/**
 * 库存服务接口
 *
 * @author ColdChain
 */
public interface InventoryService {

    /**
     * 智能库存扣减
     * <p>
     * 业务逻辑：
     * 1. 查询所有库存充足的仓库
     * 2. 使用 Haversine 公式计算距离，找到距用户最近的仓库
     * 3. 使用乐观锁扣减库存
     *
     * @param productId 商品ID
     * @param count     扣减数量
     * @param userLat   用户纬度
     * @param userLon   用户经度
     * @return 扣减结果
     */
    DeductStockResponse deductStock(Long productId, Integer count, Double userLat, Double userLon);

    /**
     * 冻结库存（TCC事务-Try阶段）
     *
     * @param productId 商品ID
     * @param count     冻结数量
     * @param userLat   用户纬度
     * @param userLon   用户经度
     * @return 冻结结果
     */
    DeductStockResponse freezeStock(Long productId, Integer count, Double userLat, Double userLon);

    /**
     * 确认扣减库存（TCC事务-Confirm阶段）
     *
     * @param inventoryId 库存ID
     * @param count       扣减数量
     * @return 是否成功
     */
    boolean confirmDeduct(Long inventoryId, Integer count);

    /**
     * 取消冻结库存（TCC事务-Cancel阶段）
     *
     * @param inventoryId 库存ID
     * @param count       释放数量
     * @return 是否成功
     */
    boolean cancelFreeze(Long inventoryId, Integer count);
}
