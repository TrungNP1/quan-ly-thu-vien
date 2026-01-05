package com.arrowhitech.tts.library.TTS12_25.controller;

import com.arrowhitech.tts.library.TTS12_25.dto.auth.*;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.enums.Role;
import com.arrowhitech.tts.library.TTS12_25.response.BaseResponse;
import com.arrowhitech.tts.library.TTS12_25.service.AuthService;
import com.arrowhitech.tts.library.TTS12_25.service.JwtService;
import com.arrowhitech.tts.library.TTS12_25.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO req) {
        User user = userService.register(
                req.getUsername(), req.getPassword(), Role.READER
        );
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Đăng kí thành công.")
                        .data(user)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<?>> login(@Valid @RequestBody LoginRequestDTO req) {
        // Xác thực thông tin đăng nhập
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUsername(),
                        req.getPassword()
                )
        );
        String accessToken = jwtService.generateAccessToken(req.getUsername());
        String refreshToken = jwtService.generateRefreshToken(req.getUsername());
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Đăng nhập thành công.")
                        .data(new LoginResponseDTO(accessToken, refreshToken))
                        .build()
        );
    }

    @PostMapping("/forget-password")
    public ResponseEntity<BaseResponse<?>> forgetPassword(@Valid @RequestBody ForgetPasswordRequestDTO req) {
        try {
            String resetToken = authService.forgetPassword(req.getUsername());
            // Luôn trả về message thành công để tránh user enumeration attack
            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Nếu tên người dùng tồn tại, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu.")
                            .data(resetToken != null ? Map.of("resetToken", resetToken) : null)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    BaseResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<?>> refresh(@Valid @RequestBody RefreshTokenRequestDTO req) {
        try {
            RefreshTokenResponseDTO responseDTO = authService.refreshToken(req.getRefreshToken());
            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Làm mới token thành công.")
                            .data(responseDTO)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    BaseResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO req) {
        try {
            authService.resetPassword(req.getResetToken(), req.getNewPassword());
            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Đặt lại mật khẩu thành công.")
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    BaseResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<BaseResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO req) {
        try {
            authService.changePassword(req);
            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Đổi mật khẩu thành công.")
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    BaseResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
