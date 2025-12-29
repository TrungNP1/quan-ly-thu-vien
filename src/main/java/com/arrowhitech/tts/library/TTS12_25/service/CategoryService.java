package com.arrowhitech.tts.library.TTS12_25.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.arrowhitech.tts.library.TTS12_25.dto.CategoryRequestDTO;
import com.arrowhitech.tts.library.TTS12_25.dto.CategoryResponseDTO;
import com.arrowhitech.tts.library.TTS12_25.entity.Category;
import com.arrowhitech.tts.library.TTS12_25.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private CategoryResponseDTO toDTO(Category cat){
        return CategoryResponseDTO.builder()
            .id(cat.getId())
            .name(cat.getName())
            .description(cat.getDescription())
            .build();
    }

    public CategoryResponseDTO create(CategoryRequestDTO dto){
        if(categoryRepository.existsByName(dto.getName())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên thể loại đã tồn tại");
        }

        Category entity = Category.builder()
        .name(dto.getName())
        .description(dto.getDescription())
        .build();

        Category saved = categoryRepository.save(entity);

        CategoryResponseDTO reponse = toDTO(saved);

        return reponse;
    }

    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto){
        Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Không tìm thấy thể loại"
        ));

        if(!category.getName().equals(dto.getName()) 
            && categoryRepository.existsByName(dto.getName())){
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên thể loại đã tồn tại");
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        categoryRepository.save(category);

        return toDTO(category);
    }

    public void delete(Long id){
        if(!categoryRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Không tìm thấy thể loại");
        }

        categoryRepository.deleteById(id);
    }

    public List<CategoryResponseDTO> getAll(){
        return categoryRepository.findAll().stream()
        .map(this :: toDTO)
        .toList();
    }
}
