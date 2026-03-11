package com.coldchain.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.inventory.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库Mapper接口
 *
 * @author Alnnt
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

}
