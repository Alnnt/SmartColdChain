package com.coldchain.inventory.service;

import com.coldchain.inventory.entity.Warehouse;

/**
 * 仓库管理服务（仅超管可写）
 */
public interface WarehouseService {

    Warehouse create(Warehouse warehouse);

    Warehouse getById(Long id);

    boolean updateById(Warehouse warehouse);

    boolean removeById(Long id);
}
