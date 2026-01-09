package com.coldchain.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private GpsLocation gps;
    private String province;
    private String city;
    private String state;
    private String detail;
    private String contactName;
    private String contactPhone;
}
