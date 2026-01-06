package com.arrowhitech.tts.library.TTS12_25.dto.loan;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequestDTO {
    @NotNull
    private List<Long> bookIds;
    
    @NotBlank(message = "Mã người dùng không được để trống")
    private String userCode;
}
