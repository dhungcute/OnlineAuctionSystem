package com.auction.core.model;
import com.auction.core.util.TimeSyncManager;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.LocalDateTime;
public abstract class Entity {
    private final String id;
    protected final LocalDateTime createdAt;
    public Entity() {
        this.id=UUID.randomUUID().toString();
        this.createdAt= TimeSyncManager.getNow();
    }
    public Entity(String id,LocalDateTime createdAt) {
            this.id=id;
            this.createdAt=createdAt;
    }
    public String getId(){
        return id;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

}
