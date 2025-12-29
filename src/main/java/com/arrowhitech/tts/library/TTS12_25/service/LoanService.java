package com.arrowhitech.tts.library.TTS12_25.service;

import com.arrowhitech.tts.library.TTS12_25.controller.LoanController;
import com.arrowhitech.tts.library.TTS12_25.entity.Loan;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;

    private Optional<User> findByUser;

}
