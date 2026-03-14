package com.coldchain.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 鍒涘缓璁㈠崟璇锋眰 DTO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    /**
     * 鍟嗗搧ID
     */
    @NotNull(message = "鍟嗗搧ID涓嶈兘涓虹┖")
    private Long productId;

    /**
     * 璐拱鏁伴噺
     */
    @NotNull(message = "璐拱鏁伴噺涓嶈兘涓虹┖")
    @Min(value = 1, message = "璐拱鏁伴噺鑷冲皯涓?")
    private Integer productCount;

    /**
     * 璁㈠崟閲戦
     */
    @NotNull(message = "璁㈠崟閲戦涓嶈兘涓虹┖")
    @DecimalMin(value = "0.01", message = "璁㈠崟閲戦蹇呴』澶т簬0")
    private BigDecimal amount;

    /**
     * 鏀惰揣鍦板潃ID
     */
    @NotNull(message = "鏀惰揣鍦板潃ID涓嶈兘涓虹┖")
    private Long addressId;
}
