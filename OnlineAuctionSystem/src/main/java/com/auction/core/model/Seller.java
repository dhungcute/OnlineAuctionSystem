package com.auction.core.model;
import java.time.LocalDateTime;

// LỚP ĐẠI DIỆN CHO TÀI KHOẢN NGƯỜI BÁN HÀNG (SELLER).
public class Seller extends User {
    private String shopName;

// Khởi tạo tài khoản người bán mới với tên cửa hàng.
    public Seller(String username, String password, String shopName) {
        super(username, password);
        this.shopName = shopName;
    }
// Tái tạo lại tài khoản người bán từ dữ liệu cũ (load từ Database).
    public Seller(String id, LocalDateTime createdAt, String username, String password,
                  String shopName) {
        super(id, createdAt, username, password);
        this.shopName = shopName;
    }
    @Override
    public String getRole() { return "Seller"; }

    public String getShopName() { return shopName; }

    public void setShopName(String name) { this.shopName = name; }
}
