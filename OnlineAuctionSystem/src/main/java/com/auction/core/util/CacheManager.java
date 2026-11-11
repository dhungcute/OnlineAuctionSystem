package com.auction.core.util;

import javafx.scene.image.Image;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

// Bộ nhớ đệm (Cache) 2 tầng lưu tạm hình ảnh trên RAM (áp dụng Singleton)
public class CacheManager {
    private static final CacheManager instance = new CacheManager();

    // Tầng 2 (Kho phụ): Dùng SoftReference để Java tự dọn bớt ảnh khi máy sắp hết RAM, tránh tràn bộ nhớ
    private final ConcurrentHashMap<String, SoftReference<Image>> imageCache = new ConcurrentHashMap<>();

    // Tầng 1 (Kho nóng): Giữ tối đa 100 ảnh được xem gần nhất (thuật toán LRU - tham số true)
    private final java.util.Map<String, Image> hardCache = java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap<String, Image>(100, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, Image> eldest) {
                    return size() > 100; // Vượt quá 100 ảnh thì tự đẩy ảnh lâu chưa xem ra ngoài
                }
            }
    );

    private CacheManager() {}

    public static CacheManager getInstance() {
        return instance;
    }

    // Lấy ảnh từ Cache: tìm ở Kho nóng trước -> không thấy thì sang Kho phụ
    public Image getImage(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        // 1. Tìm ở tầng 1 (hardCache)
        Image hot = hardCache.get(url);
        if (hot != null) {
            return hot;
        }

        // 2. Không thấy thì tìm ở tầng 2 (SoftReference)
        SoftReference<Image> ref = imageCache.get(url);
        if (ref != null) {
            Image img = ref.get();
            if (img != null) {
                hardCache.put(url, img); // Nếu còn ảnh thì đưa ngược lại lên Kho nóng
                return img;
            }
            imageCache.remove(url); // Ảnh đã bị hệ thống dọn rác thì xóa key rỗng
        }

        return null;
    }

    // Lưu ảnh mới tải vào cả 2 tầng Cache
    public void putImage(String url, Image image) {
        if (url != null && image != null) {
            imageCache.put(url, new SoftReference<>(image));
            hardCache.put(url, image);
        }
    }

    // Xóa toàn bộ bộ nhớ đệm ảnh
    public void clearCache() {
        imageCache.clear();
        hardCache.clear();
    }

    // Xóa một ảnh cụ thể khỏi Cache (dùng khi ảnh tải bị lỗi)
    public void evict(String url) {
        if (url != null) {
            imageCache.remove(url);
            hardCache.remove(url);
        }
    }
}