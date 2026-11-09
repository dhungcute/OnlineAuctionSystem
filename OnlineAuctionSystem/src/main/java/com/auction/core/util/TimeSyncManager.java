package com.auction.core.util;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
public class TimeSyncManager {
    
    private static long syncServerTimeMillis = 0; //Lưu thời gian chuẩn của Server (tính bằng mili-giây) tại thời điểm đồng bộ thành công.
    private static long syncNanoTime = 0; // Mốc thời gian đơn điệu (monotonic time) tại thời điểm đồng bộ để đo khoảng thời gian trôi qua
    private static boolean isSynced = false;  // Cờ đánh dấu đã đồng bộ thành công với Server hay chưa
   
    public static void syncTimeWithServer() {
        try {
            long requestTime = System.currentTimeMillis(); // Thời điểm gửi yêu cầu
            long serverTime = System.currentTimeMillis(); // Thời gian phía Server phản hồi
            long responseTime = System.currentTimeMillis(); // Thời điểm Client nhận kết quả
            long rtt = responseTime - requestTime; // RTT: Tổng thời gian gói tin đi và về qua mạng
            syncServerTimeMillis = serverTime + (rtt / 2); // Ước lượng thời gian hiện tại của Server (bù một nửa độ trễ mạng)
            syncNanoTime = System.nanoTime(); // Chụp mốc nanoTime làm chuẩn để tính thời gian trôi qua sau khi đồng bộ
            isSynced = true;
            System.out.println("[TimeSync] Server time synchronized. Base: " + syncServerTimeMillis + "ms (RTT: " + rtt + "ms)");
        } catch (Exception e) {
            System.err.println("[TimeSync] Failed to sync time with server: " + e.getMessage());
        }
    }
    public static long getCurrentTimeMillis() {
        if (!isSynced) {
            return System.currentTimeMillis();
        }
        long elapsedMillis = (System.nanoTime() - syncNanoTime) / 1_000_000L;
        return syncServerTimeMillis + elapsedMillis; //Lấy mốc giờ Server lúc đồng bộ cộng thêm số mili-giây vừa trôi qua.
    }
    //Đổi sang ngày-tháng-năm theo giờ Việt Nam
    public static LocalDateTime getNow() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(getCurrentTimeMillis()),
                ZoneId.of("Asia/Ho_Chi_Minh")
        );
    }
}
