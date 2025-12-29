package com.arrowhitech.tts.library.TTS12_25.dto;

import com.arrowhitech.tts.library.TTS12_25.entity.Loan;
import lombok.Data;

import java.util.List;

@Data
public class HistoryResponseDTO {
    private int page;
    private int totalPages;
    private int totalLoans;
    private List<Loan> loans;
}
