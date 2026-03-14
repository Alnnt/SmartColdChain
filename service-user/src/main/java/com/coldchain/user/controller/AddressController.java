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
    public Result<AddressDTO> getById(@Parameter(description = "地址ID") @PathVariable Long addressId) {

        Long userId = RequestUtil.getUserId();

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
    public Result<Long> create(@Valid @RequestBody AddressRequest addressRequest) {
        Long userId = RequestUtil.getUserId();
        log.info("创建地址: userId={}, contactName={}", userId, addressRequest.getContactName());
        Long addressId = addressService.create(userId, addressRequest);
        return Result.success(addressId);
    }

    @PutMapping
    @Operation(summary = "鏇存柊鍦板潃")
    public Result<Void> update(@Valid @RequestBody AddressRequest addressRequest) {
        Long userId = RequestUtil.getUserId();
        log.info("鏇存柊鍦板潃: userId={}, addressId={}", userId, addressRequest.getId());
        addressService.update(userId, addressRequest);
        return Result.success();
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "鍒犻櫎鍦板潃")
    public Result<Void> delete(@Parameter(description = "鍦板潃ID") @PathVariable Long addressId) {
        Long userId = RequestUtil.getUserId();
        log.info("鍒犻櫎鍦板潃: userId={}, addressId={}", userId, addressId);
        addressService.delete(userId, addressId);
        return Result.success();
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "璁剧疆榛樿鍦板潃")
    public Result<Void> setDefault(@Parameter(description = "鍦板潃ID") @PathVariable Long addressId) {
        Long userId = RequestUtil.getUserId();
        log.info("璁剧疆榛樿鍦板潃: userId={}, addressId={}", userId, addressId);
        addressService.setDefault(userId, addressId);
        return Result.success();
    }
}
