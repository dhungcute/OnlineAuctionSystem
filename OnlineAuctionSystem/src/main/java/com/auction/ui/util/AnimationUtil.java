package com.auction.ui.util;

import javafx.scene.paint.Color;

// Tiện ích tạo hiệu ứng sóng ánh sáng chuyển động làm nền (background) cho màn hình
public final class AnimationUtil {

    private AnimationUtil() {}

    // Tạo 3 làn sóng chuyển động nằm dưới nền của Pane (màn hình Splash / Login)
    public static void createWaveBackground(javafx.scene.layout.Pane parent) {
        // Kiểm tra nếu màn hình này đã gắn sóng rồi thì không tạo thêm nữa
        boolean alreadyInstalled = parent.getChildren().stream()
                .anyMatch(node -> node.getStyleClass().contains("wave-background-canvas"));
        if (alreadyInstalled) {
            return;
        }

        // Tạo khung vẽ Canvas tự co giãn theo kích thước cửa sổ và đặt xuống lớp dưới cùng (vị trí 0)
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas();
        canvas.getStyleClass().add("wave-background-canvas");
        canvas.widthProperty().bind(parent.widthProperty());
        canvas.heightProperty().bind(parent.heightProperty());
        canvas.setManaged(false);
        parent.getChildren().add(0, canvas);

        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Vòng lặp vẽ lại các khung hình sóng liên tục
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            private long startTime = -1;
            private long lastUpdate = 0;
            private static final long FRAME_INTERVAL = 33_000_000; // Giới hạn 30 FPS (~33ms/khung) để đỡ tốn CPU

            @Override
            public void handle(long now) {
                if (now - lastUpdate < FRAME_INTERVAL) {
                    return;
                }
                lastUpdate = now;

                if (startTime < 0) startTime = now;
                double t = (now - startTime) / 1_000_000_000.0;

                double w = canvas.getWidth();
                double h = canvas.getHeight();
                if (w < 10 || h < 10 || !canvas.isVisible()) {
                    return;
                }

                // Tô lại nền xanh đen rồi vẽ đè 3 đường sóng với màu sắc và tốc độ khác nhau
                gc.setFill(javafx.scene.paint.Color.web("#0F172A"));
                gc.fillRect(0, 0, w, h);
                drawWave(gc, w, h, t, 0.5, 0.005, h * 0.1, javafx.scene.paint.Color.web("#3B82F6", 0.2));
                drawWave(gc, w, h, t, 0.8, 0.004, h * 0.15, javafx.scene.paint.Color.web("#00f2fe", 0.15));
                drawWave(gc, w, h, t, 1.2, 0.006, h * 0.05, javafx.scene.paint.Color.web("#ffffff", 0.05));
            }
        };
        timer.start();

        // Tự động dừng hiệu ứng khi chuyển sang màn hình khác để tránh tốn RAM/CPU chạy ngầm
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                timer.stop();
            }
        });
    }

    // Vẽ 1 đường sóng uốn lượn dựa theo công thức đồ thị hình sin (Math.sin)
    private static void drawWave(javafx.scene.canvas.GraphicsContext gc, double w, double h, double time,
                                 double speed, double frequency, double amplitude, javafx.scene.paint.Color color) {
        gc.setStroke(color);
        gc.setLineWidth(2.0);
        gc.beginPath();
        for (int x = 0; x <= w; x += 20) {
            double y = h / 2.0 + Math.sin(x * frequency + time * speed) * amplitude;
            if (x == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
    }
}