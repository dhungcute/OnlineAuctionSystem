package com.auction.core.model;

// LỚP ENUM ĐỊNH NGHĨA CÁC TRẠNG THÁI CỦA MỘT PHIÊN ĐẤU GIÁ TRONG SUỐT VÒNG ĐỜI HOẠT ĐỘNG.
public enum AuctionStatus {
    @Deprecated
    PENDING,
    UPCOMING,
    OPEN,
    RUNNING,
    CLOSED,
    CANCELED
}
