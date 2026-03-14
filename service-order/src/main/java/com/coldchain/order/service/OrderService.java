package com.coldchain.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coldchain.order.dto.OrderCreateDTO;
import com.coldchain.order.dto.OrderVO;
import com.coldchain.order.entity.Order;

import java.math.BigDecimal;

/**
 * 璁㈠崟鏈嶅姟鎺ュ彛
 *
 * @author Alnnt
 */
public interface OrderService extends IService<Order> {

    /**
     * 鍒涘缓璁㈠崟锛堝垎甯冨紡浜嬪姟锛?
     *
     * @param dto 鍒涘缓璁㈠崟璇锋眰
     * @return 璁㈠崟淇℃伅
     */
    OrderVO createOrder(OrderCreateDTO dto, Long userId);

    /**
     * 鏍规嵁璁㈠崟ID鏌ヨ璁㈠崟
     *
     * @param orderId 璁㈠崟ID
     * @return 璁㈠崟淇℃伅
     */
    OrderVO getOrderById(Long orderId);

    /**
     * 鏍规嵁璁㈠崟缂栧彿鏌ヨ璁㈠崟
     *
     * @param orderNo 璁㈠崟缂栧彿
     * @return 璁㈠崟淇℃伅
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 鏍规嵁鐢ㄦ埛ID鍒嗛〉鏌ヨ璁㈠崟
     *
     * @param userId   鐢ㄦ埛ID
     * @param page     椤电爜
     * @param pageSize 姣忛〉澶у皬
     * @return 璁㈠崟鍒嗛〉鍒楄〃
     */
    IPage<OrderVO> listByUserId(Long userId, Integer page, Integer pageSize);

    /**
     * 鍙栨秷璁㈠崟
     *
     * @param orderId 璁㈠崟ID
     * @return 鏄惁鎴愬姛
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 鏀粯瀹屾垚鍥炶皟
     * 
     * 涓氬姟閫昏緫锛?
     * 1. 鏍规嵁璁㈠崟鍙锋煡璇㈣鍗?
     * 2. 楠岃瘉鏀粯閲戦鏄惁姝ｇ‘
     * 3. 鏇存柊璁㈠崟鐘舵€佷负宸叉敮浠?
     * 4. 瑙﹀彂杩愬崟鍒涘缓绛夊悗缁祦绋?
     *
     * @param orderNo    璁㈠崟鍙?
     * @param paidAmount 鏀粯閲戦
     * @return 鏄惁鎴愬姛
     */
    Boolean markOrderAsPaid(String orderNo, BigDecimal paidAmount);
}
