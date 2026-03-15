package com.coldchain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User login response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login response")
public class UserLoginResponse {

    @Schema(description = "用户ID（文本传输，避免前端 Long 溢出）")
    private String userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Access token")
    private String accessToken;

    @Schema(description = "Expires in seconds")
    private Long expiresIn;
}
