package com.coldchain.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coldchain.common.result.Result;
import com.coldchain.product.entity.Product;
import com.coldchain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品对客接口（商城浏览：分页列表、详情）
 *
 * @author ColdChain
 */
@Tag(name = "商品API", description = "商城端商品列表与详情")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ApiProductController {

    private final ProductService productService;

    @GetMapping("/page")
    @Operation(summary = "分页查询商品（瀑布流/列表）")
    public Result<IPage<Product>> page(
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        IPage<Product> data = productService.pageProducts(page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询商品详情")
    public Result<Product> getById(@Parameter(description = "商品ID（文本）") @PathVariable("id") String idStr) {
        Long id = parseId(idStr, "商品ID");
        Product product = productService.getById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.success(product);
    }

    private static Long parseId(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(name + "不能为空");
        }
        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + "格式无效");
        }
    }
}
