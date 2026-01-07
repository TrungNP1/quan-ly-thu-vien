package com.arrowhitech.tts.library.TTS12_25.controller;

import com.arrowhitech.tts.library.TTS12_25.dto.book.BookRequestDTO;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.arrowhitech.tts.library.TTS12_25.dto.book.BookResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.response.BaseResponse;
import com.arrowhitech.tts.library.TTS12_25.response.PaginationResponse;
import com.arrowhitech.tts.library.TTS12_25.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<?>> create(
            @Valid @RequestBody BookRequestDTO dto) {
        BookResponseDTO response = bookService.create(dto);

        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Tạo sách mới thành công")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<?>> update(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO dto) {
        BookResponseDTO response = bookService.update(id, dto);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Sửa sách thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'READER')")
    public ResponseEntity<BaseResponse<?>> getBookDetail(@PathVariable Long id) {
        BookResponseDTO response = bookService.getBookDetail(id);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Lấy thông tin chi tiết thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<BaseResponse<?>> getAllForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        PaginationResponse<BookResponseDTO> response = bookService.getAllForUser(pageable);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Lấy thông danh sách cho user thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<?>> getAllForAdmin(
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        if (status != null) {
            PaginationResponse<BookResponseDTO> response = bookService.getByStatus(status, pageable);
            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Lấy thông danh sách trạng thái cho admin thành công")
                            .data(response)
                            .build()
            );
        }

        PaginationResponse<BookResponseDTO> response = bookService.getAllForAdmin(pageable);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Lấy thông danh sách cho admin thành công")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<?>> deactivate(@PathVariable Long id) {
        BookResponseDTO response = bookService.deactivate(id);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Cấm phát hành sách thành công")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<?>> activate(@PathVariable Long id) {
        BookResponseDTO response = bookService.activate(id);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .status(200)
                        .message("Phát hành sách trở lại thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<?>> searchBook(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author
    ) {
        Pageable pageable = PageRequest.of(page, size);

        if (title == null && author == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập từ khóa");
        }

        if (title != null) {
            PaginationResponse<BookResponseDTO> response = bookService.searchByTitle(title, pageable);
            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Tìm kiếm thông tin sách thành công")
                            .data(response)
                            .build()
            );
        } else {
            PaginationResponse<BookResponseDTO> response = bookService.searchByAuthor(author, pageable);
            return ResponseEntity.ok(
                    BaseResponse.builder()
                            .status(200)
                            .message("Tìm kiếm thông tin sách thành công")
                            .data(response)
                            .build()
            );
        }
    }
}
