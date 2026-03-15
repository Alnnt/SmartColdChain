package com.coldchain.order.feign;

import com.coldchain.common.result.Result;
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
     * 扣减库存
     *
     * @param productId 商品ID
     * @param count     扣减数量
     * @return 操作结果
     */
    @PostMapping("/api/inventory/decrease")
    Result<Boolean> decreaseStock(@RequestParam("productId") String productId,
                                  @RequestParam("count") Integer count);

    /**
     * 回滚库存（用于补偿）
     *
     * @param productId 商品ID（文本，避免 Long 溢出）
     * @param count     回滚数量
     * @return 操作结果
     */
    @PostMapping("/api/inventory/rollback")
    Result<Boolean> rollbackStock(@RequestParam("productId") String productId,
                                  @RequestParam("count") Integer count);
}
