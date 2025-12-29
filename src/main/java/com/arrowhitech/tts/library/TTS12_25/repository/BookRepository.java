package com.arrowhitech.tts.library.TTS12_25.repository;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;
import com.arrowhitech.tts.library.TTS12_25.entity.Category;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContaining(String title);
    List<Book> findByAuthorContaining(String author);
    List<Book> findByCategoryId(Long categoryId);
    boolean existsByCategoryId(Long categoryId);
    List<Book> findByCategory(Category category);

    List<Book> findByIsActiveTrue();
    List<Book> findByIsActiveFalse();
    List<Book> findByIsActiveTrueAndTitleContaining(String title);
    List<Book> findByIsActiveTrueAndAuthorContaining(String title);
}
