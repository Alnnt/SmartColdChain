package com.coldchain.inventory.service.impl;

import com.coldchain.inventory.entity.Warehouse;
import com.coldchain.inventory.mapper.WarehouseMapper;
import com.coldchain.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 仓库管理服务实现
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseMapper warehouseMapper;

    @Override
    public Warehouse create(Warehouse warehouse) {
        warehouseMapper.insert(warehouse);
        return warehouse;
    }

    @Override
    public Warehouse getById(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public boolean updateById(Warehouse warehouse) {
        return warehouseMapper.updateById(warehouse) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return warehouseMapper.deleteById(id) > 0;
    }
}
