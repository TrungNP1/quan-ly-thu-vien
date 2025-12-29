package com.arrowhitech.tts.library.TTS12_25.controller;

import com.arrowhitech.tts.library.TTS12_25.dto.user.UpdateProfileDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.user.UserResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.response.BaseResponse;
import com.arrowhitech.tts.library.TTS12_25.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    @GetMapping("/profile")
    public ResponseEntity<BaseResponse<UserResponseDTO>> getProfile() {
        UserResponseDTO profile = service.getUserProfile();
        return ResponseEntity.ok(
                BaseResponse.<UserResponseDTO>builder()
                        .status(200)
                        .message("Lấy thông tin profile thành công.")
                        .data(profile)
                        .build()
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<BaseResponse<UserResponseDTO>> updateProfile(
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        UserResponseDTO updatedProfile = service.updateUserProfile(updateProfileDTO);
        return ResponseEntity.ok(
                BaseResponse.<UserResponseDTO>builder()
                        .status(200)
                        .message("Cập nhật profile thành công.")
                        .data(updatedProfile)
                        .build()
        );
    }

}

