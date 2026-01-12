package com.arrowhitech.tts.library.TTS12_25.service;


import com.arrowhitech.tts.library.TTS12_25.dto.auth.ChangePasswordRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.auth.RefreshTokenResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

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

        // KIỂM TRA REDIS
        String key = "revocation:user:" + username;
        String revocationTimeStr = redisTemplate.opsForValue().get(key);

        if (revocationTimeStr != null) {
            long revocationTime = Long.parseLong(revocationTimeStr);
            long refreshTokenIat = jwtService.extractIssuedAt(refreshToken);

            if (refreshTokenIat < revocationTime) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên làm việc hết hạn");
            }
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

        //Kiểm tra xem chính Reset Token này có hợp lệ so với mốc thu hồi cũ không
        String key = "revocation:user:" + username;
        String revocationTimeStr = redisTemplate.opsForValue().get(key);
        if (revocationTimeStr != null) {
            long revocationTime = Long.parseLong(revocationTimeStr);
            if (jwtService.extractIssuedAt(resetToken) < revocationTime) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Reset token đã hết hạn do có thay đổi mật khẩu trước đó.");
            }
        }

        // Thực hiện đổi mật khẩu trong DB
        userService.resetPassword(username, newPassword);

        //Cập nhật mốc thu hồi mới vào Redis
        // Việc này giúp invalidate tất cả AccessToken/RefreshToken cũ sau khi reset thành công
        long newRevocationTimestamp = (System.currentTimeMillis() / 1000) + 5;
        redisTemplate.opsForValue().set(key, String.valueOf(newRevocationTimestamp), 1, TimeUnit.DAYS);
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

        // LƯU MỐC THỜI GIAN THU HỒI VÀO REDIS
        // Lấy thời gian hiện tại (giây) + 5 giây  để xử lý Race Condition
        long revocationTimestamp = (System.currentTimeMillis() / 1000) + 5;
        String key = "revocation:user:" + user.getUsername(); // Hoặc dùng user.getId()

        // TTL là 1 ngày (bằng thời hạn Refresh Token)
        redisTemplate.opsForValue().set(key, String.valueOf(revocationTimestamp), 1, TimeUnit.DAYS);
    }
}

