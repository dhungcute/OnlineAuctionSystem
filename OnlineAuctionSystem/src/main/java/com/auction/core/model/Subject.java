package com.auction.core.model;
// GIAO DIỆN QUẢN LÝ NGƯỜI THEO DÕI, DÙNG ĐỂ THÔNG BÁO CHO CÁC THÀNH PHẦN KHÁC KHI CÓ SỰ KIỆN MỚI XẢY RA.
public interface Subject {
    // Đăng ký một người theo dõi mới vào danh sách nhận thông báo.
    void addObserver(Observer observer);
    // Hủy đăng ký, xóa người theo dõi khỏi danh sách nhận thông báo.
    void removeObserver(Observer observer);
    // Phát thông báo về lượt đặt giá mới (BidTransaction) cho tất cả những người đang theo dõi trong danh sách.
    void notifyObserver(BidTransaction newBid);
}
