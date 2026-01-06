package com.coldchain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冷链订单详情提取类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    // 货物/产品名称
    private String item;

    // 货物数量 (使用 Double 以兼容小数，若确定全是整数可改为 Integer)
    private Double quantity;

    // 计量单位
    private String unit;

    // 提货/起始地址
    private String pickupAddress;

    // 送货/目的地地址
    private String deliveryAddress;

    // 冷链特殊要求
    private String specialRequirements;

    // 预估重量
    private String estimatedWeight;

    // 联系人姓名
    private String contactName;

    // 联系人电话
    private String contactPhone;
}