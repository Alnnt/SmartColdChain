package com.coldchain.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coldchain.product.entity.Product;

/**
 * 商品服务接口
 *
 * @author ColdChain
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品（用于商城浏览，按创建时间倒序）
     */
    IPage<Product> pageProducts(Integer page, Integer pageSize);
}
