package com.auction.api.server;

import com.auction.api.protocol.NetworkRequest;
import com.auction.api.protocol.NetworkResponse;
import com.auction.core.model.*;
import com.auction.infra.*;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AuctionServer {
    public static final int PORT = 8888;

    private static final FileUserRepository userRepo = new FileUserRepository();
    private static final FileAuctionRepository auctionRepo = new FileAuctionRepository();
    private static final FileBidRepository bidRepo = new FileBidRepository();

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println("  🏛️  HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN - SERVER KHỞI ĐỘNG  ");
        System.out.println("=======================================================");

        // Tự tạo tài khoản mẫu nếu file users.dat chưa có dữ liệu
        seedInitialData();

        // Lấy và in IP để các máy khác trong mạng LAN kết nối
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            System.out.println("👉 Cổng dịch vụ (Port): " + PORT);
            System.out.println("👉 Địa chỉ máy chủ nội bộ:  127.0.0.1:" + PORT);
            System.out.println("👉 Địa chỉ Mạng LAN (Wifi): " + localIp + ":" + PORT);
            System.out.println("   (Máy khác chung Wifi hãy nhập IP: " + localIp + " để đấu giá!)");
            System.out.println("-------------------------------------------------------");
        } catch (Exception e) {
            System.out.println("[Server] Chạy trên cổng: " + PORT);
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket client = serverSocket.accept();
                // Mỗi client là một luồng (Thread) riêng biệt
                new Thread(() -> handleClient(client)).start();
            }
        } catch (IOException e) {
            System.err.println("[Server] Lỗi ServerSocket: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()))
        ) {
            NetworkRequest request = (NetworkRequest) ois.readObject();
            NetworkResponse response = processRequest(request);
            
            oos.writeObject(response);
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Server] Xử lý yêu cầu lỗi: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private static NetworkResponse processRequest(NetworkRequest req) {
        if (req == null || req.getAction() == null) {
            return NetworkResponse.fail("Yêu cầu rỗng");
        }

        try {
            switch (req.getAction()) {
                case "LOGIN": {
                    String[] creds = (String[]) req.getData();
                    Optional<User> userOpt = userRepo.findByUsername(creds[0]);
                    if (userOpt.isPresent() && userOpt.get().getPassword().equals(creds[1])) {
                        return NetworkResponse.ok("Đăng nhập thành công", userOpt.get());
                    }
                    return NetworkResponse.fail("Tên đăng nhập hoặc mật khẩu không chính xác!");
                }

                case "REGISTER": {
                    User newUser = (User) req.getData();
                    if (userRepo.existsByUsername(newUser.getUsername())) {
                        return NetworkResponse.fail("Tên người dùng đã tồn tại!");
                    }
                    userRepo.save(newUser);
                    return NetworkResponse.ok("Đăng ký tài khoản thành công!", newUser);
                }

                case "GET_ALL_AUCTIONS": {
                    List<Auction> list = auctionRepo.findAll();
                    return NetworkResponse.ok(list);
                }

                case "CREATE_AUCTION": {
                    Auction auction = (Auction) req.getData();
                    auctionRepo.save(auction);
                    return NetworkResponse.ok("Đăng phiên đấu giá thành công!", auction);
                }

                case "PLACE_BID": {
                    Object[] params = (Object[]) req.getData();
                    String auctionId = (String) params[0];
                    String bidderId = (String) params[1];
                    double amount = (Double) params[2];

                    Optional<Auction> aucOpt = auctionRepo.findById(auctionId);
                    Optional<User> userOpt = userRepo.findById(bidderId);

                    if (aucOpt.isEmpty() || userOpt.isEmpty()) {
                        return NetworkResponse.fail("Không tìm thấy phiên đấu giá hoặc người dùng!");
                    }

                    Auction auction = aucOpt.get();
                    if (!(userOpt.get() instanceof Bidder bidder)) {
                        return NetworkResponse.fail("Chỉ người mua (Bidder) mới có quyền đặt giá!");
                    }

                    // Kiểm tra phiên còn đang chạy không
                    if (auction.getStatus() != AuctionStatus.RUNNING && auction.getStatus() != AuctionStatus.OPEN) {
                        return NetworkResponse.fail("Phiên đấu giá chưa bắt đầu hoặc đã kết thúc!");
                    }

                    // Kiểm tra bước giá
                    if (amount <= auction.getHighestBid()) {
                        return NetworkResponse.fail(String.format("Giá đặt (%,.0f ₫) phải lớn hơn giá hiện tại (%,.0f ₫)!", 
                                amount, auction.getHighestBid()));
                    }

                    // Kiểm tra số dư khả dụng
                    if (bidder.getAvailableBalance() < amount) {
                        return NetworkResponse.fail(String.format("Số dư khả dụng không đủ! Hiện có: %,.0f ₫", 
                                bidder.getAvailableBalance()));
                    }

                    // 1. Tạo bản ghi lượt đặt giá mới
                    BidTransaction tx = new BidTransaction(
                            java.util.UUID.randomUUID().toString(),
                            LocalDateTime.now(),
                            bidder,
                            auction,
                            amount
                    );
                    bidRepo.save(tx);

                    // 2. Cập nhật phiên đấu giá
                    auction.setHighestBid(amount);
                    auction.injectBid(tx);
                    auctionRepo.save(auction);

                    // 3. Đóng băng số dư ví người đặt
                    bidder.setFrozenBalance(amount);
                    userRepo.updateFrozenBalance(bidder.getId(), amount);

                    return NetworkResponse.ok(String.format("Đặt giá thành công: %,.0f ₫", amount), auction);
                }

                case "GET_BIDS_BY_AUCTION": {
                    String auctionId = (String) req.getData();
                    return NetworkResponse.ok(bidRepo.findByAuctionId(auctionId));
                }

                case "TOPUP": {
                    // Nạp tiền vào tài khoản
                    Object[] params = (Object[]) req.getData();
                    String userId = (String) params[0];
                    double addAmount = (Double) params[1];
                    Optional<User> uOpt = userRepo.findById(userId);
                    if (uOpt.isPresent() && uOpt.get() instanceof Bidder b) {
                        b.addBalance(addAmount);
                        userRepo.update(b);
                        return NetworkResponse.ok("Nạp tiền thành công!", b);
                    }
                    return NetworkResponse.fail("Không tìm thấy tài khoản người mua!");
                }

                default:
                    return NetworkResponse.fail("Hành động không được hỗ trợ: " + req.getAction());
            }
        } catch (Exception e) {
            return NetworkResponse.fail("Lỗi máy chủ: " + e.getMessage());
        }
    }

    private static void seedInitialData() {
        if (userRepo.findAll().isEmpty()) {
            userRepo.save(new Admin("admin", "123"));
            userRepo.save(new Seller("carol", "123", "Carol Boutique"));
            userRepo.save(new Bidder("alice", "123", 20_000_000.0));
            userRepo.save(new Bidder("bob", "123", 10_000_000.0));
            System.out.println("[Server] Đã khởi tạo dữ liệu mẫu (admin, carol, alice, bob) mật khẩu: 123");
        }
    }
}