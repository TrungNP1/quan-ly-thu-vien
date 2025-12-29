package com.arrowhitech.tts.library.TTS12_25.service;

import com.arrowhitech.tts.library.TTS12_25.dto.user.UpdateProfileDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.user.UserResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.enums.Role;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.mapper.UserMapper;
import com.arrowhitech.tts.library.TTS12_25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@NullMarked
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }
        return auth.getName();
    }

    public User register(String username, String rawPassword, Role roleName) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại.");
        }
        User user = User.builder()
                .username(username.trim())
                .password(passwordEncoder.encode(rawPassword.trim()))
                .role(roleName)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tên người dùng."));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tài khoản không tồn tại hoặc phiên đăng nhập hết hạn"));
    }

    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy người dùng."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponseDTO getUserProfile() {
        String currentUser = getCurrentUser();
        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng")
                );
        return mapper.mapToUserResponseDTO(user);
    }


    public UserResponseDTO updateUserProfile(UpdateProfileDTO dto) {
        String currentUser = getCurrentUser();
        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng")
                );

        // 1. Kiểm tra trùng số điện thoại (Sử dụng Objects.equals để tránh NullPointerException)
        if (dto.getPhone() != null && !Objects.equals(user.getPhone(), dto.getPhone())) {
            if (userRepository.existsByPhone(dto.getPhone())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại đã tồn tại.");
            }
        }

        if (dto.getEmail() != null && !Objects.equals(user.getEmail(), dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại.");
            }
        }

        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());

        User saved = userRepository.save(user);
        return mapper.mapToUserResponseDTO(saved);
    }

}
