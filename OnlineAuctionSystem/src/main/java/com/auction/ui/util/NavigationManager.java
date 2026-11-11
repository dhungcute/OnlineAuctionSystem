package com.auction.ui.util;

import com.auction.core.util.DataReceiver;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// Bộ điều hướng chuyển qua lại giữa các màn hình trong toàn app (Singleton)
public class NavigationManager {

    private static NavigationManager instance;
    private Stage primaryStage;
    private Object currentController;
    private String currentScreen = DASHBOARD;

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String getCurrentScreen() {
        return currentScreen;
    }

    // Chuyển nhanh sang màn hình mới (không gửi kèm dữ liệu)
    public void navigateTo(String fxmlName) throws IOException {
        navigateTo(fxmlName, null, null);
    }

    // Chuyển màn hình kèm đổi tiêu đề cửa sổ và gửi dữ liệu sang màn hình mới
    public void navigateTo(String fxmlName, String title, Object data) throws IOException {

        // Dọn dẹp màn hình cũ (tắt đồng hồ/WebSocket ngầm) trước khi rời đi
        if (currentController != null) {
            try {
                currentController.getClass().getMethod("cleanup").invoke(currentController);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                System.err.println("[NavigationManager] cleanup() error: " + e.getMessage());
            }
        }

        // Nạp giao diện FXML mới và lấy Controller của nó
        this.currentScreen = fxmlName;
        FXMLLoader loader = createLoader(fxmlName);
        Parent root = loader.load();
        currentController = loader.getController();

        // Nếu có dữ liệu gửi kèm thì truyền sang màn hình mới qua DataReceiver
        if (data != null && currentController instanceof DataReceiver) {
            ((DataReceiver) currentController).receiveData(data);
        }

        // Lần đầu thì tạo cửa sổ Scene (1100x700) kèm file CSS; các lần sau chỉ đổi nội dung bên trong
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(
                    getClass().getResource("/com/auction/styles/main.css").toExternalForm());
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        // Đổi tên hiển thị trên thanh tiêu đề Windows
        if (title != null) {
            primaryStage.setTitle("Hệ thống Đấu giá - " + title);
        }

        primaryStage.setResizable(true);
        primaryStage.show();
    }

    // Đọc file .fxml từ thư mục resources/com/auction/
    public FXMLLoader createLoader(String fxmlName) {
        return new FXMLLoader(
                getClass().getResource("/com/auction/" + fxmlName + ".fxml"));
    }

    // Danh sách tên các màn hình trong app
    public static final String SPLASH = "Splash";
    public static final String LOGIN = "Login";
    public static final String REGISTER = "Register";
    public static final String DASHBOARD = "Dashboard";
    public static final String AUCTION_LIST = "AuctionList";
    public static final String AUCTION_DETAIL = "AuctionDetail";
    public static final String SELLER_MGMT = "SellerManagement";
    public static final String ADMIN_MGMT = "AdminManagement";
    public static final String USER_PROFILE = "UserProfile";
    public static final String HISTORY = "BidHistory";
    public static final String LOGOUT = "Logout";
}