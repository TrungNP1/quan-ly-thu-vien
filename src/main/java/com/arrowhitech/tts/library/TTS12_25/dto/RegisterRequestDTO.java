package com.arrowhitech.tts.library.TTS12_25.dto;

import jakarta.validation.constraints.Email;
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

    @NotBlank(message = "Họ tên không được để trống")
    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "Tên phải là chữ cái"
    )
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

}
