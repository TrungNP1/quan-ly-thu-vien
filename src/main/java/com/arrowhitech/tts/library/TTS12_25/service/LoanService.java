package com.arrowhitech.tts.library.TTS12_25.service;

import com.arrowhitech.tts.library.TTS12_25.dto.loan.LoanRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.loan.LoanResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.Loan;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.enums.LoanStatus;
import com.arrowhitech.tts.library.TTS12_25.repository.BookRepository;
import com.arrowhitech.tts.library.TTS12_25.repository.LoanRepository;
import com.arrowhitech.tts.library.TTS12_25.repository.UserRepository;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    //Hàm chung
    private LoanResponseDTO toDTO(Loan loan) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .bookId(loan.getBook().getId())
                .bookTitle(loan.getBook().getTitle())
                .userId(loan.getUser().getId())
                .userCode(loan.getUser().getCode())
                .username(loan.getUser().getUsername())
                .fullName(loan.getUser().getFullName())
                .borrowDate(loan.getBorrowDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .build();
    }

    // Admin tạo phiếu mượn
    public LoanResponseDTO loan(LoanRequestDTO dto) {
        
        User user = userRepository.findByCode(dto.getUserCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với mã: " + dto.getUserCode()));

        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        // Check thông tin liên hệ
        if (user.getPhone() == null || user.getPhone().isBlank() 
                || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Người dùng chưa khai báo thông tin liên hệ (phone/email)");
        }

        // Check sách quá hạn
        if (loanRepository.existsByUserAndStatus(user, LoanStatus.OVERDUE)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Người dùng không thể mượn vì đang có sách quá hạn chưa trả");
        }

        // Check sách khả dụng
        if (!book.getIsActive() || book.getAvailableCopies() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Sách không khả dụng");
        }

        // Check giới hạn 5 quyển
        if (loanRepository.countByUserAndStatus(user, LoanStatus.BORROWING) >= 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Người dùng đã mượn tối đa 5 quyển sách");
        }

        // Check đang mượn sách này
        if (loanRepository.findByUserAndBookAndStatus(user, book, LoanStatus.BORROWING).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Người dùng đang mượn quyển sách này rồi");
        }

        Loan entity = Loan.builder()
                .book(book)
                .user(user)
                .dueDate(LocalDateTime.now().plusDays(14))
                .status(LoanStatus.BORROWING)
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan saved = loanRepository.save(entity);

        return toDTO(saved);
    }

    //Trả sách
    public LoanResponseDTO returned(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin mượn sách"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Sách đã được trả");
        }

        Book book = loan.getBook();

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDateTime.now());
        loanRepository.save(loan);

        return toDTO(loan);
    }

    //Admin
    // Xem tất cả thông tin mượn sách
    public Page<LoanResponseDTO> getAllForAdmin(Pageable page) {
        Page<Loan> loans = loanRepository.findAll(page);
        return loans.map(this::toDTO);
    }

    //Lọc theo user
    public Page<LoanResponseDTO> getByUserId(Long userId, Pageable page) {
        Page<Loan> loans = loanRepository.findByUserId(userId, page);
        return loans.map(this::toDTO);
    }

    //Lọc theo sách
    public Page<LoanResponseDTO> getByBookId(Long bookId, Pageable page) {
        Page<Loan> loans = loanRepository.findByBookId(bookId, page);
        return loans.map(this::toDTO);
    }

    //Lọc theo trạng thái
    public Page<LoanResponseDTO> getByStatus(LoanStatus status, Pageable page) {
        Page<Loan> loans = loanRepository.findByStatus(status, page);
        return loans.map(this::toDTO);
    }

    //User
    //Xem lịch sử của mình
    public Page<LoanResponseDTO> getMyHistory(LoanStatus status, Pageable page) {
        User user = userService.getCurrentUser();
        Page<Loan> loans;

        if (status != null) {
            loans = loanRepository.findByUserAndStatus(user, status, page);
        } else {
            loans = loanRepository.findByUser(user, page);
        }
        return loans.map(this::toDTO);
    }
}
