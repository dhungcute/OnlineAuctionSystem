package com.auction.core.model;

import com.auction.core.util.TimeSyncManager;
import java.util.UUID;
import java.time.LocalDateTime;
import java.io.Serializable;

public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    protected final LocalDateTime createdAt;

    public Entity() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = com.auction.core.util.TimeSyncManager.getNow();
    }
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
