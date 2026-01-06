# API Testing Guide

## Tài khoản test
- Admin: `admin1` / `Admin@123`
- User: `user1` / `User@123`

## Cách dùng token
Sau khi login, copy `accessToken` từ response, vào tab Authorization chọn Bearer Token, paste token vào.

---

## AUTH

### Đăng ký
```
POST http://localhost:8080/api/auth/register
Body (raw JSON):
{
    "username": "newuser1",
    "password": "Test@123"
}
```

### Đăng nhập Admin
```
POST http://localhost:8080/api/auth/login
Body (raw JSON):
{
    "username": "admin1",
    "password": "Admin@123"
}
```

### Đăng nhập User
```
POST http://localhost:8080/api/auth/login
Body (raw JSON):
{
    "username": "user1",
    "password": "User@123"
}
```

### Quên mật khẩu
```
POST http://localhost:8080/api/auth/forget-password
Body (raw JSON):
{
    "username": "user1"
}
```

### Reset mật khẩu
```
POST http://localhost:8080/api/auth/reset-password
Body (raw JSON):
{
    "resetToken": "<token từ forget-password>",
    "newPassword": "NewPass@123"
}
```

### Đổi mật khẩu (cần token)
```
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer <accessToken>
Body (raw JSON):
{
    "oldPassword": "User@123",
    "newPassword": "NewPass@123"
}
```

### Refresh token
```
POST http://localhost:8080/api/auth/refresh
Body (raw JSON):
{
    "refreshToken": "<refreshToken từ login>"
}
```

---

## USER (cần token)

### Xem profile
```
GET http://localhost:8080/api/users/profile
Authorization: Bearer <accessToken>
```

### Cập nhật profile
```
PUT http://localhost:8080/api/users/profile
Authorization: Bearer <accessToken>
Body (raw JSON):
{
    "fullName": "Nguyễn Văn B",
    "phone": "0912345678",
    "email": "newmail@gmail.com"
}
```

---

## CATEGORY

### Lấy danh sách (ADMIN hoặc READER)
```
GET http://localhost:8080/api/categories
Authorization: Bearer <accessToken>
```

### Tạo mới (ADMIN)
```
POST http://localhost:8080/api/categories
Authorization: Bearer <accessToken>
Body (raw JSON):
{
    "name": "Tiểu thuyết",
    "description": "Sách tiểu thuyết"
}
```


### Sửa category (ADMIN)
```
PUT http://localhost:8080/api/categories/1
Authorization: Bearer <accessToken>
Body (raw JSON):
{
    "name": "Tiểu thuyết (Updated)",
    "description": "Mô tả mới"
}
```

### Xóa category (ADMIN)
```
DELETE http://localhost:8080/api/categories/1
Authorization: Bearer <accessToken>
```

---

## BOOK

### Lấy danh sách sách - User (READER)
```
GET http://localhost:8080/api/books?page=0&size=10
Authorization: Bearer <accessToken>
```

### Lấy danh sách sách - Admin (ADMIN)
```
GET http://localhost:8080/api/books/admin?page=0&size=10
Authorization: Bearer <accessToken>
```

### Lấy sách theo trạng thái - Admin (ADMIN)
```
GET http://localhost:8080/api/books/admin?status=true&page=0&size=10
Authorization: Bearer <accessToken>
```
```
GET http://localhost:8080/api/books/admin?status=false&page=0&size=10
Authorization: Bearer <accessToken>
```

### Xem chi tiết sách (ADMIN hoặc READER)
```
GET http://localhost:8080/api/books/1
Authorization: Bearer <accessToken>
```

### Tạo sách mới (ADMIN)
```
POST http://localhost:8080/api/books
Authorization: Bearer <accessToken>
Body (raw JSON):
{
    "categoryId": 1,
    "title": "Java Core",
    "author": "Hoàng Minh",
    "description": "Sách học Java cơ bản",
    "totalCopies": 5
}
```

### Sửa sách (ADMIN)
```
PUT http://localhost:8080/api/books/1
Authorization: Bearer <accessToken>
Body (raw JSON):
{
    "categoryId": 1,
    "title": "Java Core (Updated)",
    "author": "Hoàng Minh",
    "description": "Sách học Java - Phiên bản mới",
    "totalCopies": 10
}
```

### Ngừng phát hành sách (ADMIN)
```
PUT http://localhost:8080/api/books/1/deactivate
Authorization: Bearer <accessToken>
```

### Phát hành lại sách (ADMIN)
```
PUT http://localhost:8080/api/books/1/activate
Authorization: Bearer <accessToken>
```

### Tìm sách theo tên - User (READER)
```
GET http://localhost:8080/api/books/search?title=Java&page=0&size=10
Authorization: Bearer <accessToken>
```

### Tìm sách theo tác giả - User (READER)
```
GET http://localhost:8080/api/books/search?author=Hoàng&page=0&size=10
Authorization: Bearer <accessToken>
```

### Tìm sách theo tên - Admin (ADMIN)
```
GET http://localhost:8080/api/books/search/admin?title=Java&page=0&size=10
Authorization: Bearer <accessToken>
```

### Tìm sách theo tác giả - Admin (ADMIN)
```
GET http://localhost:8080/api/books/search/admin?author=Hoàng&page=0&size=10
Authorization: Bearer <accessToken>
```

---

## LOAN

### Mượn sách (ADMIN)
```
POST http://localhost:8080/api/loans
Authorization: Bearer <accessToken>
Body (raw JSON):
{
    "bookId": 1,
    "userCode": "US0000011"
}
```

### Trả sách (ADMIN)
```
PUT http://localhost:8080/api/loans/1/return
Authorization: Bearer <accessToken>
```

### Lấy tất cả phiếu mượn (ADMIN)
```
GET http://localhost:8080/api/loans?page=0&size=10
Authorization: Bearer <accessToken>
```

### Lọc theo userId (ADMIN)
```
GET http://localhost:8080/api/loans?userId=1&page=0&size=10
Authorization: Bearer <accessToken>
```

### Lọc theo bookId (ADMIN)
```
GET http://localhost:8080/api/loans?bookId=1&page=0&size=10
Authorization: Bearer <accessToken>
```

### Lọc theo trạng thái BORROWING (ADMIN)
```
GET http://localhost:8080/api/loans?status=BORROWING&page=0&size=10
Authorization: Bearer <accessToken>
```

### Lọc theo trạng thái RETURNED (ADMIN)
```
GET http://localhost:8080/api/loans?status=RETURNED&page=0&size=10
Authorization: Bearer <accessToken>
```

### Lọc theo trạng thái OVERDUE (ADMIN)
```
GET http://localhost:8080/api/loans?status=OVERDUE&page=0&size=10
Authorization: Bearer <accessToken>
```

### Xem lịch sử mượn của mình (READER)
```
GET http://localhost:8080/api/loans/my-history?page=0&size=10
Authorization: Bearer <accessToken>
```

### Xem lịch sử mượn theo trạng thái (READER)
```
GET http://localhost:8080/api/loans/my-history?status=BORROWING&page=0&size=10
Authorization: Bearer <accessToken>
```
```
GET http://localhost:8080/api/loans/my-history?status=RETURNED&page=0&size=10
Authorization: Bearer <accessToken>
```
