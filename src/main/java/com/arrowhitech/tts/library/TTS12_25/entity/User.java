package com.arrowhitech.tts.library.TTS12_25.entity;

import com.arrowhitech.tts.library.TTS12_25.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(unique = true, length = 10)
    private String phone;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.READER;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
