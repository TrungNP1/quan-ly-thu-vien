package com.arrowhitech.tts.library.TTS12_25.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    @NotBlank(message = "Token không được để trống")
    private String refreshToken;
}

