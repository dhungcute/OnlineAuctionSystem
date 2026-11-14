package com.auction.infra;

import com.auction.core.model.AutoBid;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JdbcAutoBidRepository {

    private static final Path FILE_PATH = Paths.get("data", "autobids.dat");
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public JdbcAutoBidRepository() {
        try {
            Path parentDir = FILE_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            System.err.println("[JdbcAutoBidRepository] Không thể tạo thư mục data: " + e.getMessage());
        }
    }

    //Lưu hoặc cập nhật cấu hình tự động đấu giá.

    public synchronized void save(AutoBid autoBid) {
        if (autoBid == null) return;
        List<AutoBid> list = readFromFile();
        boolean updated = false;

        for (int i = 0; i < list.size(); i++) {
            AutoBid ab = list.get(i);
            // Nếu trùng ID hoặc cùng người đấu giá trên 1 phiên đấu giá thì cập nhật lại giá trần
            if (ab.getId().equals(autoBid.getId()) ||
                    (ab.getAuctionId().equals(autoBid.getAuctionId()) && ab.getBidderId().equals(autoBid.getBidderId()))) {
                list.set(i, autoBid);
                updated = true;
                break;
            }
        }

        if (!updated) {
            list.add(autoBid);
        }
        writeToFile(list);
    }

    //Tìm lệnh auto-bid của 1 người cụ thể trong 1 phiên đấu giá.

    public AutoBid findByAuctionIdAndBidderId(String auctionId, String bidderId) {
        if (auctionId == null || bidderId == null) return null;
        return readFromFile().stream()
                .filter(ab -> ab.getAuctionId().equals(auctionId) && ab.getBidderId().equals(bidderId))
                .findFirst()
                .orElse(null);
    }

    //Lấy danh sách auto-bid của 1 phiên đấu giá (ưu tiên giá trần cao nhất trước).

    public List<AutoBid> findByAuctionId(String auctionId) {
        if (auctionId == null) return new ArrayList<>();
        return readFromFile().stream()
                .filter(ab -> ab.getAuctionId().equals(auctionId))
                .sorted(Comparator.comparingDouble(AutoBid::getMaxBid).reversed()
                        .thenComparing(AutoBid::getCreatedAt))
                .toList();
    }

    //Xóa lệnh auto-bid khi phiên kết thúc hoặc người dùng hủy.

    public synchronized void deleteByAuctionIdAndBidderId(String auctionId, String bidderId) {
        if (auctionId == null || bidderId == null) return;
        List<AutoBid> list = readFromFile();
        boolean removed = list.removeIf(ab -> ab.getAuctionId().equals(auctionId) && ab.getBidderId().equals(bidderId));
        if (removed) {
            writeToFile(list);
        }
    }

    // ==========================================
    // ĐỌC / GHI FILE NHỊ PHÂN AN TOÀN
    // ==========================================
    @SuppressWarnings("unchecked")
    private List<AutoBid> readFromFile() {
        lock.readLock().lock();
        try {
            if (!Files.exists(FILE_PATH) || Files.size(FILE_PATH) == 0) {
                return new ArrayList<>();
            }
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(FILE_PATH)))) {
                Object obj = ois.readObject();
                if (obj instanceof List<?> l) {
                    return new ArrayList<>((List<AutoBid>) l);
                }
            }
        } catch (EOFException ignored) {
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[JdbcAutoBidRepository] Lỗi đọc file autobids.dat: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        return new ArrayList<>();
    }

    private void writeToFile(List<AutoBid> list) {
        lock.writeLock().lock();
        try {
            Path tmpPath = FILE_PATH.resolveSibling("autobids.dat.tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmpPath)))) {
                oos.writeObject(new ArrayList<>(list));
                oos.flush();
            }
            Files.move(tmpPath, FILE_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println("[JdbcAutoBidRepository] Lỗi ghi file autobids.dat: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}