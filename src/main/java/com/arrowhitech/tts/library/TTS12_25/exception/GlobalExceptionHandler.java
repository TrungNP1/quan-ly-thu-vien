package com.arrowhitech.tts.library.TTS12_25.exception;


import com.arrowhitech.tts.library.TTS12_25.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Object>> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                BaseResponse.builder()
                        .status(400)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<Object>> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                BaseResponse.builder()
                        .status(401)
                        .message("Sai tên tài khoản hoặc mật khẩu")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        BaseResponse<Map<String, String>> response = BaseResponse.<Map<String, String>>builder()
                .status(400)
                .message("Validation failed")
                .data(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BaseResponse<Object>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(BaseResponse.builder()
                                .status(ex.getStatusCode().value())
                                .message(ex.getReason())
                                .data(null)
                                .build()
                );
    }
}
