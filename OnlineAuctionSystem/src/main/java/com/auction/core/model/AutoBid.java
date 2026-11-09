package com.auction.core.model;

import java.time.LocalDateTime;

// LỚP LƯU TRỮ CẤU HÌNH LỆNH ĐẶT GIÁ TỰ ĐỘNG CỦA NGƯỜI DÙNG CHO MỘT PHIÊN ĐẤU GIÁ CỤ THỂ.
public class AutoBid {
    private String id;
    private String auctionId;
    private String bidderId;
    private double maxBid;
    private LocalDateTime createdAt;
// Khởi tạo một cấu hình đặt giá tự động với đầy đủ thông tin chi tiết.
    public AutoBid(String id, String auctionId, String bidderId, double maxBid, LocalDateTime createdAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public String getBidderId() { return bidderId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }

    public double getMaxBid() { return maxBid; }
    public void setMaxBid(double maxBid) { this.maxBid = maxBid; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
