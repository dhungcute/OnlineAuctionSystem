package com.auction.core.model;

import com.auction.core.util.TimeSyncManager;
import java.util.UUID;
import java.time.LocalDateTime;

// Base class mang tính trừu tượng dùng làm nền tảng cho các lớp dữ liệu khác (Auction, User, Item...).
// Cung cấp sẵn mã định danh duy nhất (ID) và thời gian tạo (createdAt) để các lớp con kế thừa dùng chung, 
public abstract class Entity {
    private final String id;
    protected final LocalDateTime createdAt;

    // Khởi tạo một thực thể mới: tự động sinh ra 1 ID ngẫu nhiên không trùng lặp (UUID) và chốt thời gian tạo là thời điểm hiện tại.
    public Entity() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = com.auction.core.util.TimeSyncManager.getNow();
    }
    // Khởi tạo thực thể từ những dữ liệu có sẵn (load dữ liệu từ Database).
    public Entity(String id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
    public String getId() {
        return id;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
