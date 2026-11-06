package com.auction.core.model;
// GIAO DIỆN ĐẠI DIỆN CHO NGƯỜI THEO DÕI, DÙNG ĐỂ NHẬN THÔNG BÁO VÀ CẬP NHẬT DỮ LIỆU ĐẤU GIÁ THEO THỜI GIAN THỰC.
public interface Observer {
    // Nhận thông tin lượt đặt giá mới nhất từ Subject để cập nhật lên giao diện hoặc xử lý logic (update giá realtime).
    public void update(BidTransaction bidder);
}
