package com.coldchain.order.feign.fallback;

import com.coldchain.common.result.Result;
import com.coldchain.order.feign.InventoryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 库存服务降级处理
 *
 * @author Alnnt
 */
@Slf4j
@Component
public class InventoryClientFallback implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        log.error("库存服务调用失败: {}", cause.getMessage(), cause);

        return new InventoryClient() {
            @Override
            public Result<Boolean> decreaseStock(Long productId, Integer count) {
                log.warn("库存扣减降级: productId={}, count={}", productId, count);
                return Result.fail("库存服务暂不可用，请稍后重试");
            }

            @Override
            public Result<Boolean> rollbackStock(Long productId, Integer count) {
                log.warn("库存回滚降级: productId={}, count={}", productId, count);
                return Result.fail("库存服务暂不可用，请稍后重试");
            }
        };
    }
}
