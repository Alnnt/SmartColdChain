package com.coldchain.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpsLocation implements Serializable {
    private static final long serialVersionUID = 1L;
    private Double longitude;
    private Double latitude;
}
