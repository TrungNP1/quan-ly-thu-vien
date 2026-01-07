package com.arrowhitech.tts.library.TTS12_25.config;

import com.arrowhitech.tts.library.TTS12_25.entity.Book;
import com.arrowhitech.tts.library.TTS12_25.entity.Category;
import com.arrowhitech.tts.library.TTS12_25.entity.User;
import com.arrowhitech.tts.library.TTS12_25.enums.Role;
import com.arrowhitech.tts.library.TTS12_25.repository.BookRepository;
import com.arrowhitech.tts.library.TTS12_25.repository.CategoryRepository;
import com.arrowhitech.tts.library.TTS12_25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Password: Admin@123 (8 ký tự, có hoa, thường, số, đặc biệt)
        if (!userRepository.existsByUsername("admin1")) {
            User admin = User.builder()
                    .username("admin1")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("Administrator")
                    .phone("0900000011")
                    .email("admin1@library.com")
                    .code("AD0000011")
                    .role(Role.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
        }

        // Password: User@123 (8 ký tự, có hoa, thường, số, đặc biệt)
        if (!userRepository.existsByUsername("user1")) {
            User user = User.builder()
                    .username("user1")
                    .password(passwordEncoder.encode("User@123"))
                    .fullName("Nguyễn Văn B")
                    .phone("0900000022")
                    .email("user1@library.com")
                    .code("US0000011")
                    .role(Role.READER)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }


        // Tạo Categories
        Category catVanHoc = createCategoryIfNotExists("Văn học", "Sách văn học trong và ngoài nước");
        Category catKhoaHoc = createCategoryIfNotExists("Khoa học", "Sách khoa học tự nhiên và xã hội");
        Category catCongNghe = createCategoryIfNotExists("Công nghệ", "Sách về công nghệ thông tin, lập trình");
        Category catTruyenTranh = createCategoryIfNotExists("Truyện tranh", "Truyện với hình vẽ và nội dung phù hợp với trẻ em");
        // Tạo Books
        createBookIfNotExists("Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Tác phẩm văn học thiếu nhi nổi tiếng", 10L, catVanHoc);
        createBookIfNotExists("Clean Code", "Robert C. Martin", "Sách về kỹ thuật viết code sạch", 5L, catCongNghe);
        createBookIfNotExists("Vũ trụ trong vỏ hạt dẻ", "Stephen Hawking", "Sách khoa học phổ thông về vũ trụ", 3L, catKhoaHoc);
        createBookIfNotExists("Mắt biếc", "Nguyễn Nhật Ánh", "Tiểu thuyết văn học nổi tiếng", 6L, catVanHoc);
        createBookIfNotExists("Kafka bên bờ biển", "Murakami Haruki", "Tiểu thuyết văn học nổi tiếng của nhật bản", 4L, catVanHoc);
        createBookIfNotExists("Chú khủng long của Nobita", "Fujio F. Fujiko", "Câu chuyện dài đầu tiên của doraemon kể về cuộc hành trình trở về thời kì khủng long thống trị", 9L, catTruyenTranh);
        createBookIfNotExists("Nhật ký trong tù", "Hồ Chí Minh", "Quyển sách tuyển tập các tập thơ của Bác Hồ khi bị giam trong nhà tù Tưởng Giới Thạch", 3L, catVanHoc);
    }

    private Category createCategoryIfNotExists(String name, String description) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category cat = Category.builder()
                    .name(name)
                    .description(description)
                    .build();
            return categoryRepository.save(cat);
        });
    }

    private void createBookIfNotExists(String title, String author, String description, Long copies, Category category) {
        if (bookRepository.findByTitleAndAuthor(title, author).isEmpty()) {
            Book book = Book.builder()
                    .title(title)
                    .author(author)
                    .description(description)
                    .totalCopies(copies)
                    .availableCopies(copies)
                    .isActive(true)
                    .category(category)
                    .build();
            bookRepository.save(book);
        }
    }
}
