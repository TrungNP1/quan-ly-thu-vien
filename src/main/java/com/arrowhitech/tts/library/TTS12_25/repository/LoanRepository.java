package com.arrowhitech.tts.library.TTS12_25.repository;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;
import com.arrowhitech.tts.library.TTS12_25.entity.Loan;
import com.arrowhitech.tts.library.TTS12_25.enums.LoanStatus;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    List<Loan> findByStatus(LoanStatus status);
    long countByUserAndStatus(User user, LoanStatus status);
    boolean existsByBookId(Long bookId);
    List<Loan> findByUserId(Long userId);
    List<Loan> findByBookId(Long bookId);
    List<Loan> findByUserAndStatus(User user, LoanStatus status);
    Optional<Loan> findByUserAndBookAndStatus(User user, Book book, LoanStatus status);
}
