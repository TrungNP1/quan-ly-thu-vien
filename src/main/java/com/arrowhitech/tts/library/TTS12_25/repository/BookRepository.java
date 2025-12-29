package com.arrowhitech.tts.library.TTS12_25.repository;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;
import com.arrowhitech.tts.library.TTS12_25.entity.Category;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByCategories(Category category);
    List<Book> findByTitleContaining(String title);
    List<Book> findByAuthorsContaining(String author);
}
