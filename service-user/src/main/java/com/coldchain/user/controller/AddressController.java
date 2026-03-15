package com.coldchain.user.controller;

import com.coldchain.auth.common.JwtTokenUtil;
import com.coldchain.common.result.Result;
import com.coldchain.auth.common.util.RequestUtil;
import com.coldchain.user.dto.AddressRequest;
import com.coldchain.common.entity.AddressDTO;
import com.coldchain.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址控制器
 *
 * @author Alnnt
 */
@Slf4j
@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
@Tag(name = "地址管理", description = "用户地址的增删改查")
public class AddressController {

    private final AddressService addressService;
    private final JwtTokenUtil jwtTokenUtil;
    // private final RequestUtil requestUtil;

    @GetMapping("/list")
    @Operation(summary = "获取地址列表")
    public Result<List<AddressDTO>> list() {
        Long userId = RequestUtil.getUserId();
        List<AddressDTO> addresses = addressService.listByUserId(userId);
        return Result.success(addresses);
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "获取地址详情")
    public Result<AddressDTO> getById(@Parameter(description = "地址ID（文本）") @PathVariable("addressId") String addressIdStr) {
        Long userId = RequestUtil.getUserId();
        Long addressId = parseAddressId(addressIdStr);
        AddressDTO address = addressService.getById(userId, addressId);
        return Result.success(address);
    }

    @GetMapping("/default")
    @Operation(summary = "获取默认地址")
    public Result<AddressDTO> getDefault() {
        Long userId = RequestUtil.getUserId();
        AddressDTO address = addressService.getDefaultByUserId(userId);
        return Result.success(address);
    }

    @PostMapping
    @Operation(summary = "创建地址")
    public Result<String> create(@Valid @RequestBody AddressRequest addressRequest) {
        Long userId = RequestUtil.getUserId();
        log.info("创建地址: userId={}, contactName={}", userId, addressRequest.getContactName());
        Long addressId = addressService.create(userId, addressRequest);
        return Result.success(String.valueOf(addressId));
    }

    @PutMapping
    @Operation(summary = "更新地址")
    public Result<Void> update(@Valid @RequestBody AddressRequest addressRequest) {
        Long userId = RequestUtil.getUserId();
        log.info("更新地址: userId={}, addressId={}", userId, addressRequest.getId());
        addressService.update(userId, addressRequest);
        return Result.success();
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "删除地址")
    public Result<Void> delete(@Parameter(description = "地址ID（文本）") @PathVariable("addressId") String addressIdStr) {
        Long userId = RequestUtil.getUserId();
        Long addressId = parseAddressId(addressIdStr);
        log.info("删除地址: userId={}, addressId={}", userId, addressId);
        addressService.delete(userId, addressId);
        return Result.success();
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "设为默认地址")
    public Result<Void> setDefault(@Parameter(description = "地址ID（文本）") @PathVariable("addressId") String addressIdStr) {
        Long userId = RequestUtil.getUserId();
        Long addressId = parseAddressId(addressIdStr);
        log.info("设为默认地址: userId={}, addressId={}", userId, addressId);
        addressService.setDefault(userId, addressId);
        return Result.success();
    }

    private static Long parseAddressId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("地址ID不能为空");
        }
        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("地址ID格式无效: " + id);
        }
    }
}
