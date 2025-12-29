package com.arrowhitech.tts.library.TTS12_25.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Tên tài khoản không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu ít nhất 8 kí tự, có ít nhất 1 chữ in hoa, kí tự đặc biệt và số"
    )
    private String password;
}
