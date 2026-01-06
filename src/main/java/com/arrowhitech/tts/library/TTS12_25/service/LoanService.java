package com.arrowhitech.tts.library.TTS12_25.service;

import com.arrowhitech.tts.library.TTS12_25.dto.loan.LoanRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.loan.LoanResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.Loan;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.enums.LoanStatus;
import com.arrowhitech.tts.library.TTS12_25.repository.BookRepository;
import com.arrowhitech.tts.library.TTS12_25.repository.LoanRepository;
import com.arrowhitech.tts.library.TTS12_25.repository.UserRepository;
import com.arrowhitech.tts.library.TTS12_25.response.PaginationResponse;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public List<LoanResponseDTO> loan(LoanRequestDTO dto) {

        User user = userRepository.findByCode(dto.getUserCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với mã: " + dto.getUserCode()));

        // Check thông tin liên hệ
        if (user.getPhone() == null || user.getPhone().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Người dùng chưa khai báo thông tin liên hệ (phone/email)");
        }

        // Check sách quá hạn
        if (loanRepository.existsByUserAndStatus(user, LoanStatus.OVERDUE)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Người dùng không thể mượn vì đang có sách quá hạn chưa trả");
        }

        // Check trùng sách trong request
        Set<Long> uniqueBookIds = new HashSet<>(dto.getBookIds());
        if (uniqueBookIds.size() != dto.getBookIds().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không được mượn trùng sách trong 1 lần mượn");
        }

        // Check sách đang mượn
        List<Loan> existingLoans = loanRepository.findByUserAndBookIdInAndStatus(user, dto.getBookIds(), LoanStatus.BORROWING);
        if (!existingLoans.isEmpty()) {
            String duplicateBooks = existingLoans.stream()
                    .map(l -> l.getBook().getTitle())
                    .collect(Collectors.joining(", "));
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Đang mượn sách: " + duplicateBooks);
        }

        // Lấy sách và check tồn tại
        List<Book> books = bookRepository.findAllById(uniqueBookIds);
        if (books.size() != uniqueBookIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Một số sách không tồn tại");
        }

        // Check sách khả dụng
        List<String> unavailableBooks = books.stream()
                .filter(b -> !b.getIsActive() || b.getAvailableCopies() <= 0)
                .map(Book::getTitle)
                .toList();
        if (!unavailableBooks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sách không khả dụng: " + String.join(", ", unavailableBooks));
        }

        // Check giới hạn 5 quyển
        long currentBorrowing = loanRepository.countByUserAndStatus(user, LoanStatus.BORROWING);
        if (currentBorrowing + uniqueBookIds.size() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vượt quá giới hạn 5 quyển (đang mượn " + currentBorrowing + " quyển)");
        }

        // Tạo loans
        List<Loan> loans = new ArrayList<>();
        for (Book book : books) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);

            Loan entity = Loan.builder()
                    .book(book)
                    .user(user)
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .status(LoanStatus.BORROWING)
                    .build();
            loans.add(entity);
        }

        bookRepository.saveAll(books);
        List<Loan> savedLoans = loanRepository.saveAll(loans);

        return savedLoans.stream().map(this::toDTO).toList();
    }

    //Trả sách
    @Transactional
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
    public PaginationResponse<LoanResponseDTO> getAllForAdmin(Pageable page) {
        Page<Loan> loans = loanRepository.findAll(page);
        Page<LoanResponseDTO> dtoPage = loans.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Lọc theo user
    public PaginationResponse<LoanResponseDTO> getByUserId(Long userId, Pageable page) {
        Page<Loan> loans = loanRepository.findByUserId(userId, page);
        Page<LoanResponseDTO> dtoPage = loans.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Lọc theo sách
    public PaginationResponse<LoanResponseDTO> getByBookId(Long bookId, Pageable page) {
        Page<Loan> loans = loanRepository.findByBookId(bookId, page);
        Page<LoanResponseDTO> dtoPage = loans.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Lọc theo trạng thái
    public PaginationResponse<LoanResponseDTO> getByStatus(LoanStatus status, Pageable page) {
        Page<Loan> loans = loanRepository.findByStatus(status, page);
        Page<LoanResponseDTO> dtoPage = loans.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //User
    //Xem lịch sử của mình
    public PaginationResponse<LoanResponseDTO> getMyHistory(LoanStatus status, Pageable page) {
        User user = userService.getCurrentUser();
        Page<Loan> loans;

        if (status != null) {
            loans = loanRepository.findByUserAndStatus(user, status, page);
        } else {
            loans = loanRepository.findByUser(user, page);
        }
        Page<LoanResponseDTO> dtoPage = loans.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }
}
