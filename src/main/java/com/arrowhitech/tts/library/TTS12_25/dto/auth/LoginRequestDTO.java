package com.arrowhitech.tts.library.TTS12_25.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Tên tài khoản không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Không được phép chứa khoảng trắng và ký tự đặc biệt")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^\\S+$", message = "Không được phép chứa khoảng trắng")
    private String password;
}
