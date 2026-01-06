package com.coldchain.order.feign;

import com.coldchain.common.result.Result;
import com.coldchain.order.feign.dto.WaybillCreateDTO;
import com.coldchain.order.feign.dto.WaybillVO;
import com.coldchain.order.feign.fallback.TransportClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 运输服务 Feign 客户端
 *
 * @author ColdChain
 */
@FeignClient(
        name = "service-transport",
        contextId = "transportClient",
        fallbackFactory = TransportClientFallback.class
)
public interface TransportClient {

    /**
     * 创建运单
     *
     * @param dto 运单创建请求
     * @return 运单信息
     */
    @PostMapping("/api/waybill/create")
    Result<WaybillVO> createWaybill(@RequestBody WaybillCreateDTO dto);
}
