package com.coldchain.transport.service;

import com.coldchain.transport.entity.Driver;
import com.coldchain.transport.entity.TransportOrder;

import java.util.List;

/**
 * 运输服务接口
 *
 * @author Alnnt
 */
public interface TransportService {

    /**
     * 创建运单
     * <p>
     * 业务逻辑：
     * 1. 查找第一个空闲司机
     * 2. 锁定司机（状态改为BUSY）
     * 3. 创建运单记录
     *
     * @param orderId     订单ID
     * @param fromAddress 起始地址（仓库地址）
     * @param toAddress   目的地址（收货地址）
     * @return 创建的运单
     */
    TransportOrder createWaybill(Long orderId, String fromAddress, String toAddress);

    /**
     * 根据订单ID查询运单
     *
     * @param orderId 订单ID
     * @return 运单
     */
    TransportOrder getByOrderId(Long orderId);

    /**
     * 开始运输（司机取货后调用）
     *
     * @param transportOrderId 运单ID
     * @return 是否成功
     */
    boolean startTransport(Long transportOrderId);

    /**
     * 完成运输（司机送达后调用）
     *
     * @param transportOrderId 运单ID
     * @return 是否成功
     */
    boolean completeTransport(Long transportOrderId);

    /**
     * 取消运单
     *
     * @param transportOrderId 运单ID
     * @return 是否成功
     */
    boolean cancelTransport(Long transportOrderId);

    /**
     * 根据司机ID查询进行中的运单
     *
     * @param driverId 司机ID
     * @return 运单列表
     */
    List<TransportOrder> getActiveTransportsByDriver(Long driverId);

    /**
     * 获取所有空闲司机
     *
     * @return 空闲司机列表
     */
    List<Driver> getAvailableDrivers();

    /**
     * 更新司机位置
     *
     * @param driverId  司机ID
     * @param latitude  纬度
     * @param longitude 经度
     * @return 是否成功
     */
    boolean updateDriverLocation(Long driverId, Double latitude, Double longitude);
}
