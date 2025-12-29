package com.arrowhitech.tts.library.TTS12_25.mapper;

import com.arrowhitech.tts.library.TTS12_25.dto.user.UserResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    // Map Entity sang DTO
    public UserResponseDTO mapToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getCode(),
                user.getPhone(),
                user.getEmail()
        );
    }
}
