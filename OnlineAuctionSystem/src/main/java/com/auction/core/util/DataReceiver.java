package com.auction.core.util;

// Giao diện trung gian giúp nhận dữ liệu truyền từ màn hình cũ sang màn hình mới khi chuyển trang (NavigationManager)
public interface DataReceiver {
    // Hàm tự động được gọi ngay khi chuyển màn hình để hứng dữ liệu truyền tới
    void receiveData(Object data);
}