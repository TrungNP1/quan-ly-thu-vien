package com.arrowhitech.tts.library.TTS12_25.dto.book;

import com.arrowhitech.tts.library.TTS12_25.entity.Category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequestDTO {
    @NotNull()
    private Long categoryId;
    
    @NotBlank(message = "Tiêu đề sách không được để trống")
    private String title;
    
    @NotBlank(message = "Tên tác giả không được để trống")
    private String author;

    private String description;

    @NotNull
    @Min(0)
    private Long totalCopies;
}
