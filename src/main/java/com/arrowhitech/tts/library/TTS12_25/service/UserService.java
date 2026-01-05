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
import java.time.Year;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@NullMarked
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tên người dùng."));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông tin người dùng"));
    }

    private String generateCode() {
        int currentYear = Year.now().getValue();
        int yearSuffix = currentYear % 100;
        // Kiểm tra trùng code nếu trùng sẽ tạo lại bằng cách tăng code
        for (int attempt = 0; attempt < 10; attempt++) {
            long countInYear = userRepository.countByYear(currentYear);
            long sequenceNumber = countInYear + 1;
            String code = String.format("LIB%02d%04d", yearSuffix, sequenceNumber);
            if (!userRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Không thể tạo code duy nhất cho user");
    }

    public User register(String username, String rawPassword, Role roleName) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại.");
        }
        User user = User.builder()
                .username(username.trim())
                .password(passwordEncoder.encode(rawPassword.trim()))
                .code(generateCode())
                .role(roleName)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }


    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy người dùng."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponseDTO getUserProfile() {
        User currentUser = getCurrentUser();
        return mapper.mapToUserResponseDTO(currentUser);
    }


    public UserResponseDTO updateUserProfile(UpdateProfileDTO dto) {
        User user = getCurrentUser();
        if (dto.getFullName() != null && !dto.getFullName().isEmpty()){
            user.setFullName(dto.getFullName().trim());
        }

        // Kiểm tra trùng số điện thoại (Sử dụng Objects.equals để tránh NullPointerException)
        if (dto.getPhone() != null && !Objects.equals(user.getPhone().trim(), dto.getPhone().trim())) {
            if (userRepository.existsByPhone(dto.getPhone().trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại đã tồn tại.");
            }
            user.setPhone(dto.getPhone().trim());
        }

        if (dto.getEmail() != null && !Objects.equals(user.getEmail().trim(), dto.getEmail().trim())) {
            if (userRepository.existsByEmail(dto.getEmail().trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại.");
            }
            user.setEmail(dto.getEmail().trim());
        }

        User saved = userRepository.save(user);
        return mapper.mapToUserResponseDTO(saved);
    }

}
