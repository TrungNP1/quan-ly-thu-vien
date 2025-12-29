package com.arrowhitech.tts.library.TTS12_25.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @Pattern(regexp = "^[\\p{L}\\s]+$",
            message = "Họ và tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;

    @Pattern(regexp = "^0\\d{9}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;
}
