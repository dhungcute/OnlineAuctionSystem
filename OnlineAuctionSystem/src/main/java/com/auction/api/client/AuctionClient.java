package com.auction.api.client;

import com.auction.api.protocol.NetworkRequest;
import com.auction.api.protocol.NetworkResponse;

import java.io.*;
import java.net.Socket;

public class AuctionClient {
    // Mặc định là 127.0.0.1 (Localhost trên cùng máy). 
    // Khi sang máy khác chung Wifi, chỉ cần gọi setServerHost("192.168.x.x", 8888)
    private static String serverHost = "127.0.0.1";
    private static int serverPort = 8888;

    public static void setServerHost(String host, int port) {
        serverHost = host;
        serverPort = port;
    }

    public static String getServerHost() { return serverHost; }
    public static int getServerPort() { return serverPort; }

    /**
     * Gửi yêu cầu lên Server và nhận kết quả trả về
     */
    public static NetworkResponse send(String action, Object data) {
        try (
            Socket socket = new Socket(serverHost, serverPort);
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()))
        ) {
            // 1. Gửi gói tin NetworkRequest
            NetworkRequest request = new NetworkRequest(action, data);
            oos.writeObject(request);
            oos.flush();

            // 2. Nhận phản hồi NetworkResponse
            return (NetworkResponse) ois.readObject();

        } catch (Exception e) {
            System.err.println("[AuctionClient] Lỗi kết nối tới Server (" + serverHost + ":" + serverPort + "): " + e.getMessage());
            return NetworkResponse.fail("Không thể kết nối đến máy chủ: " + e.getMessage());
        }
    }
}