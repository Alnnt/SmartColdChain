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
 * 鍙告満瀹炰綋
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_driver")
@Schema(description = "鍙告満瀹炰綋")
public class Driver extends BaseEntity {

    /**
     * 鍙告満濮撳悕
     */
    @Schema(description = "鍙告満濮撳悕")
    private String name;

    /**
     * 鑱旂郴鐢佃瘽
     */
    @Schema(description = "鑱旂郴鐢佃瘽")
    private String phone;

    /**
     * 杞︾墝鍙?
     */
    @Schema(description = "车牌号")
    private String licensePlate;

    /**
     * 鐘舵€侊紙0-绌洪棽锛?-蹇欑锛?
     *
     * @see DriverStatus
     */
    @Schema(description = "状态（0-空闲，1-忙碌）")
    private Integer status;

    /**
     * 褰撳墠绾害
     */
    @Schema(description = "褰撳墠绾害")
    private Double latitude;

    /**
     * 褰撳墠缁忓害
     */
    @Schema(description = "褰撳墠缁忓害")
    private Double longitude;

    /**
     * 鍒ゆ柇鍙告満鏄惁绌洪棽
     */
    public boolean isFree() {
        return DriverStatus.FREE.getCode().equals(this.status);
    }

    /**
     * 鍒ゆ柇鍙告満鏄惁蹇欑
     */
    public boolean isBusy() {
        return DriverStatus.BUSY.getCode().equals(this.status);
    }
}
