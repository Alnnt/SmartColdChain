package com.coldchain.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * GPS坐标内部类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpsLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;
}