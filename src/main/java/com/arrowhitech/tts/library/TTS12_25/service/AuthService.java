package com.arrowhitech.tts.library.TTS12_25.service;


import com.arrowhitech.tts.library.TTS12_25.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;

    public LoginResponseDTO refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token không được để trống.");
        }

        if (!jwtService.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn.");
        }

        String username = jwtService.extractUsername(refreshToken);
        if (username == null) {
            throw new IllegalArgumentException("Không thể trích xuất thông tin từ refresh token.");
        }

        String newAccessToken = jwtService.generateAccessToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        return new LoginResponseDTO(newAccessToken, newRefreshToken);
    }

    public String forgetPassword(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Tên người dùng không được để trống.");
        }

        // Kiểm tra user có tồn tại không
        if (!userService.existsByUsername(username)) {
            // Không tiết lộ thông tin user có tồn tại hay không vì lý do bảo mật
            // Vẫn trả về message thành công để tránh user enumeration attack
            return null;
        }

        // Tạo reset token
        return jwtService.generateResetToken(username);
    }

    public void resetPassword(String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isEmpty()) {
            throw new IllegalArgumentException("Reset token không được để trống.");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống.");
        }

        if (!jwtService.validateToken(resetToken)) {
            throw new IllegalArgumentException("Reset token không hợp lệ hoặc đã hết hạn.");
        }

        String username = jwtService.extractUsername(resetToken);
        if (username == null) {
            throw new IllegalArgumentException("Không thể trích xuất thông tin từ reset token.");
        }

        // Reset password
        userService.resetPassword(username, newPassword);
    }
}

