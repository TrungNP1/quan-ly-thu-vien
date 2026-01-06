package com.arrowhitech.tts.library.TTS12_25.service;

import com.arrowhitech.tts.library.TTS12_25.dto.book.BookRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.book.BookResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.Book;
import com.arrowhitech.tts.library.TTS12_25.entity.Category;
import com.arrowhitech.tts.library.TTS12_25.repository.BookRepository;
import com.arrowhitech.tts.library.TTS12_25.repository.CategoryRepository;
import com.arrowhitech.tts.library.TTS12_25.response.PaginationResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    //Hàm dùng chung
    private BookResponseDTO toDTO(Book book){
        return BookResponseDTO.builder()
            .id(book.getId())
            .categoryId(book.getCategory().getId())
            .categoryName(book.getCategory().getName())
            .title(book.getTitle())
            .author(book.getAuthor())
            .totalCopies(book.getTotalCopies())
            .description(book.getDescription())
            .availableCopies(book.getAvailableCopies())
            .isActive(book.getIsActive())
            .build();
    }

    //Tạo sách mới
    public BookResponseDTO create(BookRequestDTO dto){
        Optional<Book> existingBook = bookRepository.findByTitleAndAuthor(dto.getTitle(), dto.getAuthor());

        if(existingBook.isPresent()){
            Book book = existingBook.get();

            if(book.getIsActive()){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sách đã tồn tại");
            }

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sách đã ngừng phát hành, hãy bật lại");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại"));
        
            Book entity = Book.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .category(category)
            .author(dto.getAuthor())
            .totalCopies(dto.getTotalCopies())
            .availableCopies(dto.getTotalCopies())
            .build();

            Book saved = bookRepository.save(entity);

            return toDTO(saved);
    }

    //Sửa sách
    public BookResponseDTO update(Long id, BookRequestDTO dto){
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));
        
        Optional<Book> duplicate = bookRepository.findByTitleAndAuthor(dto.getTitle(), dto.getAuthor());

        if(duplicate.isPresent() && !duplicate.get().getId().equals(id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sách với tiêu đề và tác giả này đã tồn tại");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại"));

        Long borrowed = book.getTotalCopies() - book.getAvailableCopies();

        if(dto.getTotalCopies() < borrowed){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể giảm số lượng tổng nhỏ hơn số sách đang cho mượn");
        }

        Long newAvailable = dto.getTotalCopies() - borrowed;

        book.setCategory(category);
        book.setAuthor(dto.getAuthor());
        book.setTitle(dto.getTitle());
        book.setDescription(dto.getDescription());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(newAvailable);

        bookRepository.save(book);
        
        return toDTO(book);
    }

    //Ngừng phát hành
    public BookResponseDTO deactivate(Long id){
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        if(!book.getIsActive()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sách đã ngừng phát hành, không cần thay đổi");
        }
            
        book.setIsActive(false);
        bookRepository.save(book);
        return toDTO(book);
    }

    //Phát hành trở lại
    public BookResponseDTO activate(Long id){
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        if(book.getIsActive()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sách đang phát hành, không cần thay đổi");
        }
        
        book.setIsActive(true);
        bookRepository.save(book);
        return toDTO(book);
    }

    //Xem chi tiết
    public BookResponseDTO getBookDetail(Long id){
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        return toDTO(book);    
    }

    //Lấy danh sách
    //Dùng cho user
    public PaginationResponse<BookResponseDTO> getAllForUser(Pageable page){
        Page<Book> books = bookRepository.findAvailableBooks(page);
        Page<BookResponseDTO> dtoPage = books.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Dùng cho admin
    public PaginationResponse<BookResponseDTO> getAllForAdmin(Pageable page){
        Page<Book> books = bookRepository.findAll(page);
        Page<BookResponseDTO> dtoPage = books.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Dùng cho admin - Lấy theo trạng thái
    public PaginationResponse<BookResponseDTO> getByStatus(Boolean status, Pageable page){
        Page<Book> books = bookRepository.findByIsActive(status, page);
        Page<BookResponseDTO> dtoPage = books.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Tìm kiếm
    //Tên sách cho User
    public PaginationResponse<BookResponseDTO> searchTitleForUser(String title, Pageable page){
        Page<Book> books = bookRepository.findAvailableBooksByTitle(title, page);
        Page<BookResponseDTO> dtoPage = books.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Tên sách cho Admin
    public PaginationResponse<BookResponseDTO> searchTitleForAdmin(String title, Pageable page){
        Page<Book> books = bookRepository.findByTitleContainingIgnoreCase(title, page);
        Page<BookResponseDTO> dtoPage = books.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Tên tác giả cho User
    public PaginationResponse<BookResponseDTO> searchAuthorForUser(String author, Pageable page){
        Page<Book> books = bookRepository.findAvailableBooksByAuthor(author, page);
        Page<BookResponseDTO> dtoPage = books.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }

    //Tên tác giả cho Admin
    public PaginationResponse<BookResponseDTO> searchAuthorForAdmin(String author, Pageable page){
        Page<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author, page);
        Page<BookResponseDTO> dtoPage = books.map(this::toDTO);
        return PaginationResponse.from(dtoPage);
    }
}
