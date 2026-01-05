package com.arrowhitech.tts.library.TTS12_25.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenResponseDTO {
    private String newAccessToken;
    private String newRefreshToken;
}
