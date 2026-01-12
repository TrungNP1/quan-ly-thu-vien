package com.arrowhitech.tts.library.TTS12_25.service;


import com.arrowhitech.tts.library.TTS12_25.dto.auth.ChangePasswordRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.auth.RefreshTokenResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.repository.UserRepository;
import jakarta.transaction.Transactional;
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
            try {
                long revocationTime = Long.parseLong(revocationTimeStr);
                long refreshTokenIat = jwtService.extractIssuedAt(refreshToken);

                if (refreshTokenIat < revocationTime) {
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "Phiên làm việc hết hạn");
                }
            } catch (Exception e) {
                // Log error, nhưng có thể cho phép refresh nếu data bị corrupt
                // Hoặc throw exception tùy theo security policy
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi xác thực token");
            }
        }

        String newAccessToken = jwtService.generateAccessToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
    }


    @Transactional // Đảm bảo tính nhất quán
    public void resetPassword(String resetToken, String newPassword) {

        // 2. Validate Token về mặt chữ ký và thời gian hết hạn (Expired)
        if (!jwtService.validateToken(resetToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ.");
        }

        String username = jwtService.extractUsername(resetToken);
        String key = "revocation:user:" + username;

        //KIỂM TRA TRƯỚC: Token này có được tạo ra TRƯỚC mốc thu hồi gần nhất không?
        String lastRevocationStr = redisTemplate.opsForValue().get(key);
        if (lastRevocationStr != null) {
            long lastRevocation = Long.parseLong(lastRevocationStr);
            // Nếu Token phát hành trước thời điểm thu hồi -> Token đã bị vô hiệu hóa
            if (jwtService.extractIssuedAt(resetToken) < lastRevocation) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Link reset này đã bị vô hiệu hóa.");
            }
        }

        // THỰC HIỆN ĐỔI MẬT KHẨU TRONG DB TRƯỚC
        // Nếu dòng này lỗi, Transaction sẽ rollback, Redis bên dưới sẽ không bị update
        userService.resetPassword(username, newPassword);

        //bCẬP NHẬT MỐC THU HỒI MỚI VÀO REDIS
        // Vô hiệu hóa tất cả Access/Refresh/Reset token cũ đang tồn tại
        try {
            long newRevocationTimestamp = (System.currentTimeMillis() / 1000);
            redisTemplate.opsForValue().set(key, String.valueOf(newRevocationTimestamp), 1, TimeUnit.DAYS);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống bảo mật.");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDTO req) {
        User user = userService.getCurrentUser();

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        // Cập nhật mật khẩu trong DB trước (trong Transaction)
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        // Cập nhật Redis ở bước cuối cùng
        String key = "revocation:user:" + user.getUsername();
        long revocationTimestamp = System.currentTimeMillis() / 1000;

        try {
            redisTemplate.opsForValue().set(key, String.valueOf(revocationTimestamp),
                    1, TimeUnit.DAYS);
        } catch (Exception e) {
            // Rollback DB nếu không thể bảo mật phiên làm việc
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể vô hiệu hóa phiên cũ. Vui lòng thử lại.");
        }
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
}

