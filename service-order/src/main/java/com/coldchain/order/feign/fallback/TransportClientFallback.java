package com.coldchain.order.feign.fallback;

import com.coldchain.common.result.Result;
import com.coldchain.order.feign.TransportClient;
import com.coldchain.order.feign.dto.WaybillCreateDTO;
import com.coldchain.order.feign.dto.WaybillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 运输服务降级处理
 *
 * @author Alnnt
 */
@Slf4j
@Component
public class TransportClientFallback implements FallbackFactory<TransportClient> {

    @Override
    public TransportClient create(Throwable cause) {
        log.error("运输服务调用失败: {}", cause.getMessage(), cause);

        return new TransportClient() {
            @Override
            public Result<WaybillVO> createWaybill(WaybillCreateDTO dto) {
                log.warn("创建运单降级: orderId={}", dto.getOrderId());
                return Result.fail("运输服务暂不可用，请稍后重试");
            }
        };
    }
}
