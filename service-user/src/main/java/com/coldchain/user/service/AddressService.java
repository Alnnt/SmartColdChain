package com.coldchain.user.service;

import com.coldchain.user.dto.AddressRequest;
import com.coldchain.common.entity.AddressDTO;

import java.util.List;

/**
 * 鐢ㄦ埛鍦板潃鏈嶅姟鎺ュ彛
 *
 * @author Alnnt
 */
public interface AddressService {

    /**
     * 鑾峰彇鐢ㄦ埛鍦板潃鍒楄〃
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鍦板潃鍒楄〃
     */
    List<AddressDTO> listByUserId(Long userId);

    /**
     * 鑾峰彇鍦板潃璇︽儏
     *
     * @param userId    鐢ㄦ埛ID
     * @param addressId 鍦板潃ID
     * @return 鍦板潃璇︽儏
     */
    AddressDTO getById(Long userId, Long addressId);

    /**
     * 鑾峰彇鐢ㄦ埛榛樿鍦板潃
     *
     * @param userId 鐢ㄦ埛ID
     * @return 榛樿鍦板潃
     */
    AddressDTO getDefaultByUserId(Long userId);

    /**
     * 鍒涘缓鍦板潃
     *
     * @param userId  鐢ㄦ埛ID
     * @param request 鍦板潃璇锋眰
     * @return 鍦板潃ID
     */
    Long create(Long userId, AddressRequest request);

    /**
     * 鏇存柊鍦板潃
     *
     * @param userId  鐢ㄦ埛ID
     * @param request 鍦板潃璇锋眰
     */
    void update(Long userId, AddressRequest request);

    /**
     * 鍒犻櫎鍦板潃
     *
     * @param userId    鐢ㄦ埛ID
     * @param addressId 鍦板潃ID
     */
    void delete(Long userId, Long addressId);

    /**
     * 璁剧疆榛樿鍦板潃
     *
     * @param userId    鐢ㄦ埛ID
     * @param addressId 鍦板潃ID
     */
    void setDefault(Long userId, Long addressId);
}
