package com.auction.infra;

import com.auction.core.model.Auction;
import com.auction.core.model.AuctionStatus;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JdbcAuctionRepository {

    private static final Path FILE_PATH = Paths.get("data", "auctions.dat");
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public JdbcAuctionRepository() {
        try {
            Path parentDir = FILE_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            System.err.println("[JdbcAuctionRepository] Không thể tạo thư mục data: " + e.getMessage());
        }
    }

    //Lưu phiên đấu giá mới hoặc cập nhật phiên hiện có.

    public synchronized boolean save(Auction auction) {
        if (auction == null) return false;
        List<Auction> list = readFromFile();
        boolean found = false;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(auction.getId())) {
                list.set(i, auction);
                found = true;
                break;
            }
        }

        if (!found) {
            list.add(auction);
        }

        writeToFile(list);
        return true;
    }

    //Lấy toàn bộ danh sách phiên đấu giá (mới nhất lên đầu).

    public List<Auction> findAll() {
        return readFromFile().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    //Tìm phiên đấu giá theo ID.

    public Optional<Auction> findById(String id) {
        if (id == null) return Optional.empty();
        return readFromFile().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    //Lấy danh sách phiên đấu giá của 1 người bán (Seller).

    public List<Auction> findBySellerId(String sellerId) {
        if (sellerId == null) return new ArrayList<>();
        return readFromFile().stream()
                .filter(a -> a.getSeller() != null && a.getSeller().getId().equals(sellerId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    //Cập nhật nhanh trạng thái, giá cao nhất và thời gian bắt đầu của phiên.

    public synchronized void updateStatus(String auctionId, AuctionStatus newStatus, double highestBid, LocalDateTime startTime) {
        if (auctionId == null) return;
        List<Auction> list = readFromFile();
        boolean updated = false;

        for (Auction a : list) {
            if (a.getId().equals(auctionId)) {
                a.setStatus(newStatus);
                a.setHighestBid(highestBid);
                if (startTime != null) {
                    a.setStartTime(startTime);
                }
                updated = true;
                break;
            }
        }

        if (updated) {
            writeToFile(list);
        }
    }

    //Cập nhật thời gian kết thúc phiên

    public synchronized void updateEndTime(String auctionId, LocalDateTime endTime) {
        if (auctionId == null || endTime == null) return;
        List<Auction> list = readFromFile();
        boolean updated = false;

        for (Auction a : list) {
            if (a.getId().equals(auctionId)) {
                a.setEndTime(endTime);
                updated = true;
                break;
            }
        }

        if (updated) {
            writeToFile(list);
        }
    }

    /**
     * Xóa phiên đấu giá theo ID.
     */
    public synchronized boolean deleteById(String id) {
        if (id == null) return false;
        List<Auction> list = readFromFile();
        boolean removed = list.removeIf(a -> a.getId().equals(id));
        if (removed) {
            writeToFile(list);
        }
        return removed;
    }

    // ==========================================
    // ĐỌC / GHI FILE NHỊ PHÂN AN TOÀN
    // ==========================================
    @SuppressWarnings("unchecked")
    private List<Auction> readFromFile() {
        lock.readLock().lock();
        try {
            if (!Files.exists(FILE_PATH) || Files.size(FILE_PATH) == 0) {
                return new ArrayList<>();
            }
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(FILE_PATH)))) {
                Object obj = ois.readObject();
                if (obj instanceof List<?> l) {
                    List<Auction> auctions = (List<Auction>) l;
                    // ĐẢM BẢO KHỞI TẠO LẠI OBSERVERS ĐỂ KHÔNG BỊ NullPointerException
                    for (Auction a : auctions) {
                        a.initTransientFields();
                    }
                    return new ArrayList<>(auctions);
                }
            }
        } catch (EOFException ignored) {
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[JdbcAuctionRepository] Lỗi đọc file auctions.dat: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        return new ArrayList<>();
    }

    private void writeToFile(List<Auction> list) {
        lock.writeLock().lock();
        try {
            Path tmpPath = FILE_PATH.resolveSibling("auctions.dat.tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmpPath)))) {
                oos.writeObject(new ArrayList<>(list));
                oos.flush();
            }
            Files.move(tmpPath, FILE_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println("[JdbcAuctionRepository] Lỗi ghi file auctions.dat: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}