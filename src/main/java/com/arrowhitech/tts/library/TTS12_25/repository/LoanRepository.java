package com.arrowhitech.tts.library.TTS12_25.repository;
import com.arrowhitech.tts.library.TTS12_25.entity.Loan;
import com.arrowhitech.tts.library.TTS12_25.enums.LoanStatus;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {
    List<User> findByUser(User user);
    List<Loan> findByStatus(LoanStatus status);
    int countByUserAndStatus(User user, LoanStatus status);
}
