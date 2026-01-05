package com.arrowhitech.tts.library.TTS12_25.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arrowhitech.tts.library.TTS12_25.dto.category.CategoryRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.category.CategoryResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.response.BaseResponse;
import com.arrowhitech.tts.library.TTS12_25.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping()
    public ResponseEntity<BaseResponse<?>> create(
        @Valid @RequestBody CategoryRequestDTO dto){
            CategoryResponseDTO response = categoryService.create(dto);
            return ResponseEntity.ok(
                BaseResponse.builder()
                .status(200)
                .message("Tạo thể loại mới thành công")
                .data(response)
                .build()
            );
        }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<?>> update(
        @PathVariable Long id,
        @Valid @RequestBody CategoryRequestDTO dto){
            CategoryResponseDTO response = categoryService.update(id, dto);
            return ResponseEntity.ok(
                BaseResponse.builder()
                .status(200)
                .message("Sửa thể loại thành công")
                .data(response)
                .build()
            );
        }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<?>> delete(@PathVariable Long id){
        categoryService.delete(id);
        return ResponseEntity.ok(
            BaseResponse.builder()
            .status(200)
            .message("Xóa thể loại thành công")
            .build()
        );
    }

    @GetMapping
    public ResponseEntity<BaseResponse<?>> getAll(){
        List<CategoryResponseDTO> catList = categoryService.getAll();
        return ResponseEntity.ok(
            BaseResponse.builder()
            .status(200)
            .message("Lấy danh sách thư mục thành công")
            .data(catList)
            .build()
        );
    }

}
