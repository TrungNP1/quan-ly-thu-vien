package com.arrowhitech.tts.library.TTS12_25.controller;

import com.arrowhitech.tts.library.TTS12_25.dto.loan.LoanRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.loan.LoanResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.enums.LoanStatus;
import com.arrowhitech.tts.library.TTS12_25.response.BaseResponse;
import com.arrowhitech.tts.library.TTS12_25.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping()
    public ResponseEntity<BaseResponse<?>> loan(
            @Valid @RequestBody LoanRequestDTO dto
    ){
        LoanResponseDTO response = loanService.loan(dto);

        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Mượn thành công!")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BaseResponse<?>> returned(
            @PathVariable Long id
    ){
        LoanResponseDTO response = loanService.returned(id);

        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Trả sách thành công!")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<BaseResponse<?>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) LoanStatus status
            ){
        Pageable pageable = PageRequest.of(page, size);

        if(userId != null){
            Page<LoanResponseDTO> response = loanService.getByUserId(userId, pageable);

            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Lấy dữ liệu thành công")
                            .data(response)
                            .build()
            );
        }

        if(bookId != null){
            Page<LoanResponseDTO> response = loanService.getByBookId(bookId, pageable);

            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Lấy dữ liệu thành công")
                            .data(response)
                            .build()
            );
        }

        if(status != null){
            Page<LoanResponseDTO> response = loanService.getByStatus(status, pageable);

            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Lấy dữ liệu thành công")
                            .data(response)
                            .build()
            );
        }

        Page<LoanResponseDTO> response = loanService.getAllForAdmin(pageable);

        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Lấy dữ liệu thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/my-history")
    public ResponseEntity<BaseResponse<?>> getMyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LoanStatus status
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanResponseDTO> response = loanService.getMyHistory(status, pageable);

        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Lấy dữ liệu thành công")
                        .data(response)
                        .build()
        );
    }
}
