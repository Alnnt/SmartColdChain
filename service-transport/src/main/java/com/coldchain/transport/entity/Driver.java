package com.coldchain.transport.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.coldchain.common.entity.BaseEntity;
import com.coldchain.transport.enums.DriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 司机实体
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_driver")
@Schema(description = "司机实体")
public class Driver extends BaseEntity {

    /**
     * 司机姓名
     */
    @Schema(description = "司机姓名")
    private String name;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 车牌号
     */
    @Schema(description = "车牌号")
    private String licensePlate;

    /**
     * 状态（0-空闲，1-忙碌）
     *
     * @see DriverStatus
     */
    @Schema(description = "状态（0-空闲，1-忙碌）")
    private Integer status;

    /**
     * 当前纬度
     */
    @Schema(description = "当前纬度")
    private Double latitude;

    /**
     * 当前经度
     */
    @Schema(description = "当前经度")
    private Double longitude;

    /**
     * 判断司机是否空闲
     */
    public boolean isFree() {
        return DriverStatus.FREE.getCode().equals(this.status);
    }

    /**
     * 判断司机是否忙碌
     */
    public boolean isBusy() {
        return DriverStatus.BUSY.getCode().equals(this.status);
    }
}
