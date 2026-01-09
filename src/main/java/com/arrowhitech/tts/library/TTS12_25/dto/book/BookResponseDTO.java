package com.arrowhitech.tts.library.TTS12_25.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String author;
    private String description;
    private Long totalCopies;
    private Long availableCopies;
    Boolean isActive;
}
