package com.coldchain.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.coldchain.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 仓库实体
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_warehouse")
@Schema(description = "仓库实体")
public class Warehouse extends BaseEntity {

    /**
     * 仓库名称
     */
    @Schema(description = "仓库名称")
    private String name;

    /**
     * 仓库地址
     */
    @Schema(description = "仓库地址")
    private String address;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private Double latitude;

    /**
     * 经度
     */
    @Schema(description = "经度")
    private Double longitude;
}
