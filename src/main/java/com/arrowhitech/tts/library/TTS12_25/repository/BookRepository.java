package com.arrowhitech.tts.library.TTS12_25.repository;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    //Kiểm tra
    Optional<Book> findByTitleAndAuthor(String title, String author);
    boolean existsByCategoryId(Long categoryId);

    //User
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable page);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.availableCopies > 0 AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findAvailableBooksByTitle(@Param("title") String title, Pageable page);
    
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.availableCopies > 0 AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    Page<Book> findAvailableBooksByAuthor(@Param("author") String author, Pageable page);

    //Admin
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable page);
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable page);
    Page<Book> findByIsActive(Boolean status, Pageable page);

}
