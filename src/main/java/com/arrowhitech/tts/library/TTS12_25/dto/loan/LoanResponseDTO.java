package com.arrowhitech.tts.library.TTS12_25.dto.loan;

import java.time.LocalDateTime;
import com.arrowhitech.tts.library.TTS12_25.enums.LoanStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanResponseDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String username;
    private Long userId;
    private String fullName;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus  status;
}
