package com.coldchain.order.feign;

import com.coldchain.common.result.Result;
import com.coldchain.order.feign.dto.DecreaseStockVO;
import com.coldchain.order.feign.fallback.InventoryClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务 Feign 客户端
 *
 * @author Alnnt
 */
@FeignClient(name = "service-inventory", contextId = "inventoryClient", fallbackFactory = InventoryClientFallback.class)
public interface InventoryClient {

    /**
     * 冻结库存（下单时预占，返回库存ID与仓库ID；支付时再确认扣减）
     *
     * @param productId 商品ID
     * @param count     数量
     * @return 操作结果（含 success、inventoryId、warehouseId）
     */
    @PostMapping("/api/inventory/freeze")
    Result<DecreaseStockVO> freezeStock(@RequestParam("productId") String productId,
                                         @RequestParam("count") Integer count);

    /**
     * 确认扣减（支付成功时：冻结转实际扣减）
     *
     * @param inventoryId 库存记录ID
     * @param count       数量
     * @return 操作结果
     */
    @PostMapping("/api/inventory/confirm-deduct")
    Result<Boolean> confirmDeduct(@RequestParam("inventoryId") String inventoryId,
                                  @RequestParam("count") Integer count);

    /**
     * 取消冻结（订单取消时释放预占）
     *
     * @param inventoryId 库存记录ID
     * @param count       数量
     * @return 操作结果
     */
    @PostMapping("/api/inventory/cancel-freeze")
    Result<Boolean> cancelFreeze(@RequestParam("inventoryId") String inventoryId,
                                 @RequestParam("count") Integer count);

    /**
     * 扣减库存（保留用于兼容；当前流程使用冻结+确认扣减）
     */
    @PostMapping("/api/inventory/decrease")
    Result<DecreaseStockVO> decreaseStock(@RequestParam("productId") String productId,
                                          @RequestParam("count") Integer count);

    /**
     * 回滚库存（用于未走冻结流程的补偿）
     */
    @PostMapping("/api/inventory/rollback")
    Result<Boolean> rollbackStock(@RequestParam("productId") String productId,
                                  @RequestParam("count") Integer count);
}
