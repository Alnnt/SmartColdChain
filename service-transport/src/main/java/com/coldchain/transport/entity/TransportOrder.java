package com.coldchain.transport.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.coldchain.common.entity.BaseEntity;
import com.coldchain.transport.enums.TransportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 运单实体
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_transport_order")
@Schema(description = "运单实体")
public class TransportOrder extends BaseEntity {

    /**
     * 关联的订单ID（来自订单服务）
     */
    @Schema(description = "关联的订单ID")
    private Long orderId;

    /**
     * 司机ID
     */
    @Schema(description = "司机ID")
    private Long driverId;

    /**
     * 起始地址（仓库地址）
     */
    @Schema(description = "起始地址")
    private String startAddress;

    /**
     * 目的地址（收货地址）
     */
    @Schema(description = "目的地址")
    private String endAddress;

    /**
     * 运单状态（0-待取货，1-运输中，2-已送达，3-已取消）
     *
     * @see TransportStatus
     */
    @Schema(description = "运单状态（0-待取货，1-运输中，2-已送达）")
    private Integer status;

    /**
     * 预计到达时间
     */
    @Schema(description = "预计到达时间")
    private LocalDateTime estimatedArrivalTime;

    /**
     * 实际到达时间
     */
    @Schema(description = "实际到达时间")
    private LocalDateTime actualArrivalTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
