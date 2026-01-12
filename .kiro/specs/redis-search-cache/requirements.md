# Requirements: Redis Cache cho Book Search

## Giới thiệu

Implement tính năng cache kết quả tìm kiếm sách sử dụng Redis + Spring Cache, giúp giảm tải database khi nhiều users search cùng từ khóa.

## Glossary

- **Redis**: In-memory database, lưu data trong RAM, tốc độ đọc ~0.1ms (nhanh hơn MySQL ~100 lần)
- **Spring Cache**: Framework cung cấp annotations (@Cacheable, @CacheEvict) để cache kết quả method
- **Cache Key**: Định danh duy nhất cho mỗi entry trong cache (VD: "book-search:harry:page0")
- **TTL (Time To Live)**: Thời gian cache tồn tại trước khi tự động xóa
- **Cache Eviction**: Xóa cache khi data thay đổi

---

## Hiện trạng dự án

### Đã có sẵn:
- ✅ Redis service trong `docker-compose.yml` (port 6379)
- ✅ Dependency `spring-boot-starter-data-redis` trong `pom.xml`
- ❌ Chưa config Redis trong `application.yaml`
- ❌ Chưa có `@EnableCaching`
- ❌ Chưa có `@Cacheable` trên các method search

### Methods cần cache (trong BookService):
```java
searchByTitle(String title, Pageable page)      // Tìm theo tên sách
searchByAuthor(String author, Pageable page)    // Tìm theo tác giả
getAllForUser(Pageable page)                    // Lấy danh sách sách cho user
getAllForAdmin(Pageable page)                   // Lấy danh sách sách cho admin
```

### Methods cần xóa cache (khi data thay đổi):
```java
create(BookRequestDTO dto)      // Tạo sách mới
update(Long id, BookRequestDTO) // Cập nhật sách
activate(Long id)               // Bật sách
deactivate(Long id)             // Tắt sách
```

---

## Requirements

### Requirement 1: Cấu hình Redis Connection

**User Story:** As a developer, I want to configure Redis connection, so that Spring can connect to Redis server.

#### Acceptance Criteria

1. WHEN application starts, THE System SHALL connect to Redis server at localhost:6379
2. WHEN Redis connection fails, THE System SHALL log error and continue running (graceful degradation)

---

### Requirement 2: Enable Spring Cache với Redis

**User Story:** As a developer, I want to enable Spring Cache with Redis as storage, so that cache data persists across app restarts.

#### Acceptance Criteria

1. THE System SHALL use Redis as cache storage (not in-memory)
2. THE System SHALL serialize cache data as JSON (human-readable)
3. THE System SHALL set default TTL = 10 minutes for all cache entries
4. WHEN cache entry expires, THE System SHALL automatically remove it

---

### Requirement 3: Cache Book Search Results

**User Story:** As a user, I want search results to be cached, so that repeated searches are faster.

#### Acceptance Criteria

1. WHEN user searches by title, THE System SHALL cache the result with key pattern: `book-search:title:{title}:page:{pageNumber}`
2. WHEN user searches by author, THE System SHALL cache the result with key pattern: `book-search:author:{author}:page:{pageNumber}`
3. WHEN same search is performed again, THE System SHALL return cached result without querying database
4. THE Cache SHALL differentiate between Admin and User results (different cache keys)

---

### Requirement 4: Cache Book List Results

**User Story:** As a user, I want book list to be cached, so that browsing is faster.

#### Acceptance Criteria

1. WHEN user requests book list, THE System SHALL cache with key: `book-list:user:page:{pageNumber}`
2. WHEN admin requests book list, THE System SHALL cache with key: `book-list:admin:page:{pageNumber}`
3. WHEN filtering by status, THE System SHALL cache with key: `book-list:status:{status}:page:{pageNumber}`

---

### Requirement 5: Cache Eviction on Data Change

**User Story:** As a developer, I want cache to be cleared when book data changes, so that users always see up-to-date data.

#### Acceptance Criteria

1. WHEN a new book is created, THE System SHALL clear all book-related caches
2. WHEN a book is updated, THE System SHALL clear all book-related caches
3. WHEN a book is activated/deactivated, THE System SHALL clear all book-related caches
4. THE System SHALL use cache prefix `book-*` to clear all related caches at once

---

### Requirement 6: DTO Serialization

**User Story:** As a developer, I want DTOs to be serializable, so that they can be stored in Redis.

#### Acceptance Criteria

1. THE BookResponseDTO SHALL implement Serializable interface
2. THE PaginationResponse SHALL implement Serializable interface
3. THE System SHALL handle serialization/deserialization automatically via Jackson

---

## Flow Diagram

```
User search "Harry Potter"
        │
        ▼
┌─────────────────────────────┐
│  Spring Cache Interceptor   │
│  Check key: "book-search:   │
│  title:harry potter:page:0" │
└─────────────────────────────┘
        │
        ▼
   ┌─────────┐
   │ Redis   │
   └─────────┘
        │
   ┌────┴────┐
   │         │
 HIT       MISS
   │         │
   ▼         ▼
Return    Query DB
from      Map to DTO
cache     Save to Redis
          Return
```

---

## Files cần tạo/sửa

| File | Action | Mô tả |
|------|--------|-------|
| `application.yaml` | SỬA | Thêm Redis config |
| `config/RedisConfig.java` | TẠO | Config Redis + Cache |
| `dto/book/BookResponseDTO.java` | SỬA | Thêm Serializable |
| `response/PaginationResponse.java` | SỬA | Thêm Serializable |
| `service/BookService.java` | SỬA | Thêm @Cacheable, @CacheEvict |

---

## Cache Strategy Summary

| Method | Cache Name | Key Pattern | TTL |
|--------|------------|-------------|-----|
| `searchByTitle` | book-search | `title:{title}:page:{page}` | 10 min |
| `searchByAuthor` | book-search | `author:{author}:page:{page}` | 10 min |
| `getAllForUser` | book-list | `user:page:{page}` | 10 min |
| `getAllForAdmin` | book-list | `admin:page:{page}` | 10 min |
| `getByStatus` | book-list | `status:{status}:page:{page}` | 10 min |
| `create/update/activate/deactivate` | - | Evict all `book-*` | - |
