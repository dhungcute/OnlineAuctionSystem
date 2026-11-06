package com.auction.core.model;
import java.time.LocalDateTime;

// LỚP TRỪU TƯỢNG ĐẠI DIỆN CHO NGƯỜI DÙNG CHUNG TRONG HỆ THỐNG.
public abstract class User extends Entity {
    private String username;
    private String password;

// Khởi tạo tài khoản người dùng mới.
    public User(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }
// Tái tạo lại tài khoản người dùng từ dữ liệu cũ (Khôi phục thông tin Database).
    public User(String id, LocalDateTime createdAt, String username, String password) {
        super(id, createdAt);
        this.username = username;
        this.password = password;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    public abstract String getRole();
}
