package com.arrowhitech.tts.library.TTS12_25.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String code;
    private String phone;
    private String email;
}
