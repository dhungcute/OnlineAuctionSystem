package com.auction.infra;

import com.auction.core.model.BidTransaction;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//Repository quản lý lịch sử các lượt đặt giá


public class FileBidRepository {

    private static final Path FILE_PATH = Paths.get("data", "bids.dat");
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileBidRepository() {
        // Tự động khởi tạo thư mục ./data/ nếu chưa có
        try {
            Path parentDir = FILE_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            System.err.println("Không thể tạo thư mục data: " + e.getMessage());
        }
    }

    //Ghi nhận một giao dịch đặt giá mới (Append-only: chỉ thêm, không sửa xóa).

    public synchronized void save(BidTransaction tx) {
        if (tx == null) return;
        List<BidTransaction> list = readFromFile();

        // Kiểm tra tránh trùng lặp ID giao dịch
        boolean exists = list.stream().anyMatch(b -> b.getId().equals(tx.getId()));
        if (!exists) {
            list.add(tx);
            writeToFile(list);
        }
    }

    // Lấy toàn bộ lịch sử đặt giá trên hệ thống (sắp xếp mới nhất lên đầu).

    public List<BidTransaction> findAll() {
        return readFromFile().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();
    }

    //Lấy tất cả lượt đặt giá của một phiên đấu giá cụ thể (sắp xếp tăng dần theo thời gian).

    public List<BidTransaction> findByAuctionId(String auctionId) {
        if (auctionId == null) return new ArrayList<>();
        return readFromFile().stream()
                .filter(b -> b.getAuction() != null && auctionId.equals(b.getAuction().getId()))
                .sorted(Comparator.comparing(BidTransaction::getTimestamp))
                .toList();
    }

    //Lấy tất cả lượt đặt giá của một người dùng cụ thể (sắp xếp mới nhất lên đầu).

    public List<BidTransaction> findByBidderId(String bidderId) {
        if (bidderId == null) return new ArrayList<>();
        return readFromFile().stream()
                .filter(b -> b.getBidder() != null && bidderId.equals(b.getBidder().getId()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();
    }

    //Tìm một giao dịch đặt giá theo ID.

    public Optional<BidTransaction> findById(String id) {
        if (id == null) return Optional.empty();
        return readFromFile().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();
    }

    // =========================================================================
    // CÁC HÀM XỬ LÝ SERIALIZATION FILE NHỊ PHÂN AN TOÀN ĐA LUỒNG
    // =========================================================================

    @SuppressWarnings("unchecked")
    private List<BidTransaction> readFromFile() {
        lock.readLock().lock();
        try {
            if (!Files.exists(FILE_PATH) || Files.size(FILE_PATH) == 0) {
                return new ArrayList<>();
            }
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(FILE_PATH)))) {
                Object obj = ois.readObject();
                if (obj instanceof List<?> l) {
                    return new ArrayList<>((List<BidTransaction>) l);
                }
            }
        } catch (EOFException ignored) {
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Lỗi đọc file bids.dat: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        return new ArrayList<>();
    }

    private void writeToFile(List<BidTransaction> list) {
        lock.writeLock().lock();
        try {
            // Ghi ra file tạm .tmp trước rồi mới hoán đổi nguyên tử để chống hỏng file nếu crash đột ngột
            Path tmpPath = FILE_PATH.resolveSibling("bids.dat.tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmpPath)))) {
                oos.writeObject(new ArrayList<>(list));
                oos.flush();
            }
            Files.move(tmpPath, FILE_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println("Lỗi ghi file bids.dat: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}