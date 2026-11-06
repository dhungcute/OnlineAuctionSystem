package com.auction.core.model;
import java.time.LocalDateTime;

// LỚP ĐẠI DIỆN CHO TÀI KHOẢN QUẢN TRỊ VIÊN (ADMIN)
public class Admin extends User {
    // Khởi tạo một tài khoản Admin mới với tên đăng nhập và mật khẩu.
    public Admin(String username, String password) {
        super(username, password);
    }
    // Khởi tạo tài khoản Admin từ dữ liệu đã có (load từ Database).
    public Admin(String id, LocalDateTime createdAt, String username, String password) {
        super(id, createdAt, username, password);
    }
    // Trả về chuỗi "Admin" để hệ thống nhận diện quyền hạn phân quyền của người dùng.
    @Override
    public String getRole() { return "Admin"; }
}
