package com.arrowhitech.tts.library.TTS12_25.dto;

import com.arrowhitech.tts.library.TTS12_25.entity.Category;
import jakarta.persistence.Column;

public class BookRequestDTO {
    private Category category;
    private String title;
    private String author;
}
