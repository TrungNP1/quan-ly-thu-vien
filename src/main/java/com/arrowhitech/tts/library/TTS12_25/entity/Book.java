package com.arrowhitech.tts.library.TTS12_25.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "book")
    private List<Loan> loans;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "So luong khong duoc de trong")
    @Min(value = 0, message = "So luong khong duoc am")
    @Column(nullable = false)
    private Long totalCopies;

    @NotNull(message = "So luong con lai khong duoc de trong")
    @Min(value = 0, message = "So luong khong duoc am")
    @Column(nullable = false)
    private Long availableCopies;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

}
