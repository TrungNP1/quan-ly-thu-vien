package com.arrowhitech.tts.library.TTS12_25.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Tên tài khoản không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Tài khoản chỉ được chứa kí tự thường, in hoa, số và '_'")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^\\S+$", message = "Mật khẩu không được phép chứa khoảng trắng")
    private String password;
}
