package com.auction.core.model;

import com.auction.core.util.TimeSyncManager;
import java.util.UUID;
import java.time.LocalDateTime;
import java.io.Serializable;

public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    protected final LocalDateTime createdAt;

    // Khởi tạo một thực thể mới: tự động sinh ra 1 ID ngẫu nhiên không trùng lặp (UUID) và chốt thời gian tạo là thời điểm hiện tại.
    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = TimeSyncManager.getNow();
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
