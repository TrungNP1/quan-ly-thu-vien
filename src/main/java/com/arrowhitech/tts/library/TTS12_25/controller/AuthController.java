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
                req.getUsername().trim(), req.getPassword().trim(), Role.READER
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

}
