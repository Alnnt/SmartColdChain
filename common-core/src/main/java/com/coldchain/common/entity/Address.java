package com.coldchain.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    //经纬度
    private GpsLocation gps;

    // 详细地址
    private String province;
    private String city;
    private String state;
    private String detail;

    // 收件人信息
    private String contactName;
    private String contactPhone;
}
