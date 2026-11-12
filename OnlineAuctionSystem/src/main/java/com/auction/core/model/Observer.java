package com.auction.core.model;

// Lớp phụ trách update giá realtime
public interface Observer {
    // Nhận thông tin lượt đặt giá mới nhất từ Subject để cập nhật lên giao diện hoặc xử lý logic (update giá realtime).
    void update(BidTransaction bidder);
}
