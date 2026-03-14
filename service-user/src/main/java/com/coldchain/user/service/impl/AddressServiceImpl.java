package com.coldchain.user.service.impl;

import com.coldchain.common.exception.BusinessException;
import com.coldchain.common.result.ResultCode;
import com.coldchain.user.dto.AddressRequest;
import com.coldchain.common.entity.AddressDTO;
import com.coldchain.user.entity.UserAddress;
import com.coldchain.user.mapper.UserAddressMapper;
import com.coldchain.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户地址服务实现类
 *
 * @author Alnnt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final UserAddressMapper addressMapper;

    @Override
    public List<AddressDTO> listByUserId(Long userId) {
        List<UserAddress> addresses = addressMapper.selectByUserId(userId);
        return addresses.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getById(Long userId, Long addressId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地址不存在");
        }
        return convertToVO(address);
    }

    @Override
    public AddressDTO getDefaultByUserId(Long userId) {
        UserAddress address = addressMapper.selectDefaultByUserId(userId);
        if (address == null) {
            return null;
        }
        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, AddressRequest request) {
        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressMapper.cancelDefaultByUserId(userId);
        }

        UserAddress address = UserAddress.builder()
                .userId(userId)
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detail(request.getDetail())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0)
                .build();

        addressMapper.insert(address);
        log.info("用户地址创建成功: userId={}, addressId={}", userId, address.getId());
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, AddressRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "地址ID不能为空");
        }

        UserAddress address = addressMapper.selectById(request.getId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地址不存在");
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressMapper.cancelDefaultByUserId(userId);
        }

        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setLongitude(request.getLongitude());
        address.setLatitude(request.getLatitude());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0);
        addressMapper.updateById(address);
        log.info("用户地址更新成功: userId={}, addressId={}", userId, request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long addressId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地址不存在");
        }

        addressMapper.deleteById(addressId);
        log.info("用户地址删除成功: userId={}, addressId={}", userId, addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地址不存在");
        }

        // 先取消所有默认地址
        addressMapper.cancelDefaultByUserId(userId);

        // 设置新的默认地址
        address.setIsDefault(1);
        addressMapper.updateById(address);
        log.info("设置默认地址成功: userId={}, addressId={}", userId, addressId);
    }

    /**
     * 转换为VO对象
     */
    private AddressDTO convertToVO(UserAddress address) {
        String fullAddress = address.getProvince() + address.getCity() +
                address.getDistrict() + address.getDetail();

        return AddressDTO.builder()
                .id(address.getId())
                .contactName(address.getContactName())
                .contactPhone(address.getContactPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detail(address.getDetail())
                .fullAddress(fullAddress)
                .longitude(address.getLongitude())
                .latitude(address.getLatitude())
                .isDefault(address.getIsDefault() == 1)
                .build();
    }
}
