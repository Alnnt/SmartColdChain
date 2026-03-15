package com.coldchain.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coldchain.common.result.Result;
import com.coldchain.product.dto.ProductDTO;
import com.coldchain.product.entity.Product;
import com.coldchain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理端接口（商品管理员、超级管理员 CRUD）
 *
 * @author ColdChain
 */
@Tag(name = "商品管理端", description = "商品CRUD，供商品管理员与超级管理员使用")
@RestController
@RequestMapping("/api/product/manager")
@RequiredArgsConstructor
public class ProductManagerController {

    private final ProductService productService;

    @GetMapping("/page")
    @Operation(summary = "分页查询商品")
    public Result<IPage<Product>> page(
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        IPage<Product> data = productService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize));
        return Result.success(data);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询商品")
    public Result<Product> getById(@Parameter(description = "商品ID（文本）") @PathVariable("id") String idStr) {
        Long id = parseId(idStr, "商品ID");
        Product product = productService.getById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.success(product);
    }

    @PostMapping
    @Operation(summary = "新增商品")
    public Result<Product> create(@Valid @RequestBody ProductDTO dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .img(dto.getImg())
                .build();
        productService.save(product);
        return Result.success("创建成功", product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public Result<Product> update(
            @Parameter(description = "商品ID（文本）") @PathVariable("id") String idStr,
            @Valid @RequestBody ProductDTO dto) {
        Long id = parseId(idStr, "商品ID");
        Product product = productService.getById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setImg(dto.getImg());
        productService.updateById(product);
        return Result.success("更新成功", product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Boolean> delete(@Parameter(description = "商品ID（文本）") @PathVariable("id") String idStr) {
        Long id = parseId(idStr, "商品ID");
        boolean ok = productService.removeById(id);
        return ok ? Result.success("删除成功", true) : Result.fail("删除失败");
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
