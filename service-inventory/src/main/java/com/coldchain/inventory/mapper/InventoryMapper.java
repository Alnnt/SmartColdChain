package com.coldchain.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.inventory.dto.WarehouseStockDTO;
import com.coldchain.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 库存Mapper接口
 *
 * @author Alnnt
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 查询指定商品在各仓库的可用库存（联表查询）
     * 筛选条件：可用库存 >= 需要扣减的数量
     *
     * @param productId 商品ID
     * @param count     需要扣减的数量
     * @return 仓库库存列表
     */
    @Select("""
            SELECT
                i.id AS inventoryId,
                i.warehouse_id AS warehouseId,
                w.name AS warehouseName,
                w.latitude AS latitude,
                w.longitude AS longitude,
                (i.total_stock - i.frozen_stock) AS availableStock,
                i.total_stock AS totalStock
            FROM t_inventory i
            INNER JOIN t_warehouse w ON i.warehouse_id = w.id
            WHERE i.product_id = #{productId}
              AND i.deleted = 0
              AND w.deleted = 0
              AND (i.total_stock - i.frozen_stock) >= #{count}
            """)
    List<WarehouseStockDTO> findWarehousesWithStock(@Param("productId") Long productId,
            @Param("count") Integer count);

    /**
     * 乐观锁扣减库存
     * 使用 CAS 方式确保并发安全：只有当前库存 >= 扣减数量时才能扣减成功
     *
     * @param inventoryId 库存ID
     * @param count       扣减数量
     * @return 受影响的行数（1表示成功，0表示失败）
     */
    @Update("""
            UPDATE t_inventory
            SET total_stock = total_stock - #{count},
                update_time = NOW()
            WHERE id = #{inventoryId}
              AND deleted = 0
              AND total_stock >= #{count}
            """)
    int deductStockWithOptimisticLock(@Param("inventoryId") Long inventoryId,
            @Param("count") Integer count);

    /**
     * 冻结库存（预扣库存，用于分布式事务的Try阶段）
     *
     * @param inventoryId 库存ID
     * @param count       冻结数量
     * @return 受影响的行数
     */
    @Update("""
            UPDATE t_inventory
            SET frozen_stock = frozen_stock + #{count},
                update_time = NOW()
            WHERE id = #{inventoryId}
              AND deleted = 0
              AND (total_stock - frozen_stock) >= #{count}
            """)
    int freezeStock(@Param("inventoryId") Long inventoryId,
            @Param("count") Integer count);

    /**
     * 释放冻结库存（用于分布式事务的Cancel阶段）
     *
     * @param inventoryId 库存ID
     * @param count       释放数量
     * @return 受影响的行数
     */
    @Update("""
            UPDATE t_inventory
            SET frozen_stock = frozen_stock - #{count},
                update_time = NOW()
            WHERE id = #{inventoryId}
              AND deleted = 0
              AND frozen_stock >= #{count}
            """)
    int unfreezeStock(@Param("inventoryId") Long inventoryId,
            @Param("count") Integer count);

    /**
     * 确认扣减库存（用于分布式事务的Confirm阶段）
     * 从总库存和冻结库存同时扣减
     *
     * @param inventoryId 库存ID
     * @param count       扣减数量
     * @return 受影响的行数
     */
    @Update("""
            UPDATE t_inventory
            SET total_stock = total_stock - #{count},
                frozen_stock = frozen_stock - #{count},
                update_time = NOW()
            WHERE id = #{inventoryId}
              AND deleted = 0
              AND total_stock >= #{count}
              AND frozen_stock >= #{count}
            """)
    int confirmDeductStock(@Param("inventoryId") Long inventoryId,
            @Param("count") Integer count);
}
