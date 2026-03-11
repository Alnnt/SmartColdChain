package com.coldchain.user.service;

import com.coldchain.user.dto.AddressRequest;
import com.coldchain.common.entity.AddressDTO;

import java.util.List;

/**
 * 用户地址服务接口
 *
 * @author Alnnt
 */
public interface AddressService {

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<AddressDTO> listByUserId(Long userId);

    /**
     * 获取地址详情
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址详情
     */
    AddressDTO getById(Long userId, Long addressId);

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    AddressDTO getDefaultByUserId(Long userId);

    /**
     * 创建地址
     *
     * @param userId  用户ID
     * @param request 地址请求
     * @return 地址ID
     */
    Long create(Long userId, AddressRequest request);

    /**
     * 更新地址
     *
     * @param userId  用户ID
     * @param request 地址请求
     */
    void update(Long userId, AddressRequest request);

    /**
     * 删除地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     */
    void delete(Long userId, Long addressId);

    /**
     * 设置默认地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     */
    void setDefault(Long userId, Long addressId);
}
