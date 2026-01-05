package com.arrowhitech.tts.library.TTS12_25.service;


import com.arrowhitech.tts.library.TTS12_25.dto.auth.ChangePasswordRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.auth.RefreshTokenResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RefreshTokenResponseDTO refreshToken(String refreshToken) {
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
        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
    }

    public String forgetPassword(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Tên người dùng không được để trống.");
        }
        // Kiểm tra user có tồn tại không
        if (!userService.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Tên tài khoản không tồn tại");
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

    public void changePassword(ChangePasswordRequestDTO req){
        User user = userService.getCurrentUser();

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())){
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        if (req.getOldPassword().equals(req.getNewPassword())){
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}

