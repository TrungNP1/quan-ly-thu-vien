package com.arrowhitech.tts.library.TTS12_25.dto.loan;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequestDTO {
    @NotNull
    private Long bookId;
}
