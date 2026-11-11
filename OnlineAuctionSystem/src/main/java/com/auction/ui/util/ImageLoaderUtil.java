package com.auction.ui.util;

import com.auction.core.util.CacheManager;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

// Tiện ích tải, lưu đệm (Cache) và thu nhỏ/mã hóa hình ảnh sản phẩm
public final class ImageLoaderUtil {

    private ImageLoaderUtil() {}

    // Tải ảnh ở chế độ chạy ngầm (không làm đơ giao diện khi lướt danh sách)
    public static Image loadItemImage(String imageUrl, double width, double height) {
        return loadItemImage(imageUrl, width, height, true);
    }

    // Tải ảnh đồng bộ (chờ tải xong ngay lập tức)
    public static Image loadItemImageSync(String imageUrl, double width, double height) {
        return loadItemImage(imageUrl, width, height, false);
    }

    // Hàm xử lý tải ảnh chung: ưu tiên lấy từ Cache -> đọc từ Base64 / Link mạng / File máy
    private static Image loadItemImage(String imageUrl, double width, double height, boolean backgroundLoading) {
        String url = imageUrl == null ? "" : imageUrl.trim();
        if (url.isEmpty()) return createFallbackImage((int) width, (int) height);

        // Nếu là chuỗi Base64 dài thì băm thành mã ngắn để làm key lưu Cache cho gọn
        String keyBase = url;
        if (url.startsWith("data:image/") && url.contains(";base64,")) {
            keyBase = fastHash(url);
        }

        // Kiểm tra xem ảnh (đúng kích thước này) đã có sẵn trong Cache chưa
        String cacheKey = keyBase + "_" + (int) width + "_" + (int) height;
        Image cached = CacheManager.getInstance().getImage(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            if (url.startsWith("data:image/") && url.contains(";base64,")) {
                // Trường hợp 1: Ảnh mã hóa Base64 -> giải mã ra file tạm rồi load lên JavaFX
                File tempFile = new File(System.getProperty("java.io.tmpdir"), "img_" + keyBase + ".tmp");
                if (!tempFile.exists()) {
                    String base64Data = url.substring(url.indexOf(";base64,") + 8);
                    byte[] decoded = Base64.getDecoder().decode(base64Data);
                    Files.write(tempFile.toPath(), decoded);
                }
                Image img = new Image(tempFile.toURI().toString(), width, height, true, true, backgroundLoading);

                // Nếu tải ngầm bị lỗi thì tự xóa khỏi Cache
                if (backgroundLoading) {
                    final String key = cacheKey;
                    img.errorProperty().addListener((obs, wasErr, isErr) -> {
                        if (isErr) {
                            CacheManager.getInstance().evict(key);
                            System.err.println("[ImageLoaderUtil] Async load error, evicted cache key: " + key);
                        }
                    });
                }
                CacheManager.getInstance().putImage(cacheKey, img);
                return img;

            } else if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) {
                // Trường hợp 2: Ảnh từ đường dẫn mạng (HTTP/HTTPS) hoặc URL file
                Image remote = new Image(url, width, height, true, true, backgroundLoading);
                if (backgroundLoading) {
                    final String key = cacheKey;
                    remote.errorProperty().addListener((obs, wasErr, isErr) -> {
                        if (isErr) {
                            CacheManager.getInstance().evict(key);
                            System.err.println("[ImageLoaderUtil] Async load error, evicted cache key: " + key);
                        }
                    });
                    CacheManager.getInstance().putImage(cacheKey, remote);
                } else {
                    if (!remote.isError()) {
                        CacheManager.getInstance().putImage(cacheKey, remote);
                    }
                }
                return remote;

            } else {
                // Trường hợp 3: Đường dẫn file ảnh nội bộ trên ổ cứng
                File localFile = new File(url);
                if (localFile.exists()) {
                    Image local = new Image(localFile.toURI().toString(), width, height, true, true, backgroundLoading);
                    if (!local.isError()) {
                        CacheManager.getInstance().putImage(cacheKey, local);
                        return local;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ImageLoaderUtil] Error loading image: " + e.getMessage());
        }
        return createFallbackImage((int) width, (int) height); // Lỗi hoặc không có ảnh thì trả về ảnh trống
    }

    // Thu nhỏ ảnh (tối đa 600px), nén thành JPG và mã hóa sang chuỗi Base64 để gửi qua mạng
    public static String convertFileToBase64AndResize(File file) {
        try {
            BufferedImage original = ImageIO.read(file);
            if (original == null) return "";

            // Tính lại kích thước mới giữ đúng tỉ lệ, cạnh dài nhất không quá 600px
            int targetWidth = original.getWidth();
            int targetHeight = original.getHeight();
            int maxDim = 600;

            if (targetWidth > maxDim || targetHeight > maxDim) {
                if (targetWidth > targetHeight) {
                    targetHeight = (targetHeight * maxDim) / targetWidth;
                    targetWidth = maxDim;
                } else {
                    targetWidth = (targetWidth * maxDim) / targetHeight;
                    targetHeight = maxDim;
                }
            }

            // Vẽ lại ảnh mới trên nền trắng (tránh lỗi nền đen khi ảnh PNG trong suốt chuyển sang JPG)
            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, targetWidth, targetHeight);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // Xuất ra định dạng JPG rồi mã hóa thành chuỗi Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/jpeg;base64," + base64;
        } catch (Exception e) {
            System.err.println("[ImageLoaderUtil] Error encoding/resizing image: " + e.getMessage());
            // Nếu nén thất bại thì đọc thẳng file gốc chuyển sang Base64
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                return "data:image/jpeg;base64," + base64;
            } catch (Exception ex) {
                return "";
            }
        }
    }

    // Băm chuỗi dài thành mã hex ngắn gọn để đặt tên file tạm / key Cache
    private static String fastHash(String input) {
        return Integer.toHexString(input.hashCode());
    }

    // Tạo một bức ảnh trong suốt rỗng làm ảnh dự phòng (tránh lỗi NullPointerException trên UI)
    private static Image createFallbackImage(int width, int height) {
        int w = Math.max(width, 1);
        int h = Math.max(height, 1);
        WritableImage image = new WritableImage(w, h);
        PixelWriter px = image.getPixelWriter();
        Color bg = Color.TRANSPARENT;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                px.setColor(x, y, bg);
            }
        }
        return image;
    }
}