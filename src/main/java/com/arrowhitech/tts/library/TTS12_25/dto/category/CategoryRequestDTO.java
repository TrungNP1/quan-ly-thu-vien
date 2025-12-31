package com.arrowhitech.tts.library.TTS12_25.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    @NotBlank(message = "Tên thể loại không được để trống")
    private String name;
    private String description;
}
