package com.arrowhitech.tts.library.TTS12_25.repository;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;
import com.arrowhitech.tts.library.TTS12_25.entity.Loan;
import com.arrowhitech.tts.library.TTS12_25.enums.LoanStatus;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Page<Loan> findByUser(User user, Pageable page);
    Page<Loan> findByStatus(LoanStatus status, Pageable page);
    Page<Loan> findByUserId(Long userId, Pageable page);
    Page<Loan> findByBookId(Long bookId, Pageable page);
    Page<Loan> findByUserAndStatus(User user, LoanStatus status, Pageable page);
    Optional<Loan> findByUserAndBookAndStatus(User user, Book book, LoanStatus status);
    long countByUserAndStatus(User user, LoanStatus status);
    boolean existsByUserAndStatus(User user, LoanStatus status);
}
