package com.auction.ui.support.ui;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

// INTERFACE ĐỊNH NGHĨA TIÊU CHUẨN ĐỂ CHẠY CÁC TÁC VỤ NGẦM, GIÚP GIAO DIỆN JAVAFX KHÔNG BỊ ĐƠ KHI XỬ LÝ NẶNG HOẶC GỌI API.
public interface BackgroundTaskRunner {
    // Khởi chạy một tác vụ ngầm với tên luồng (thread) cụ thể, trả kết quả qua hàm onSuccess nếu thành công và báo lỗi qua onFailure nếu thất bại.
    <T> void run(String threadName, Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onFailure);
}