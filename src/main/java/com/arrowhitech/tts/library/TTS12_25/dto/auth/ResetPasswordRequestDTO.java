package com.arrowhitech.tts.library.TTS12_25.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String resetToken;
    private String newPassword;
}

