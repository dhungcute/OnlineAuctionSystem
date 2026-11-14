package com.auction.infra;

import com.auction.core.model.Bidder;
import com.auction.core.model.User;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;


//Repository class quản lý người dùng bằng Serialization (lưu vào file users.dat).

public class FileUserRepository {

    // Đường dẫn lưu file dữ liệu users.dat trong thư mục ./data/
    private static final Path FILE_PATH = Paths.get("data", "users.dat");
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileUserRepository() {
        try {
            Path parentDir = FILE_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            System.err.println("Không thể tạo thư mục data: " + e.getMessage());
        }
    }
    //Thêm mới người dùng vào file users.dat (nếu chưa trùng ID hoặc Username).

    public synchronized void save(User user) {
        if (user == null) return;
        List<User> users = readFromFile();

        // Kiểm tra tránh trùng lặp ID hoặc Username
        boolean exists = users.stream().anyMatch(u ->
                u.getId().equals(user.getId()) || u.getUsername().equalsIgnoreCase(user.getUsername())
        );

        if (!exists) {
            users.add(user);
            writeToFile(users);
        }
    }
    //Lấy toàn bộ danh sách người dùng từ file.

    public List<User> findAll() {
        return readFromFile();
    }

    // Tìm người dùng theo ID duy nhất.
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return readFromFile().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    //Tìm người dùng theo username (không phân biệt chữ hoa, chữ thường).

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return readFromFile().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
    public synchronized void update(User user) {
        if (user == null) return;
        List<User> users = readFromFile();
        boolean found = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                found = true;
                break;
            }
        }

        if (found) {
            writeToFile(users);
        }
    }

    // Cập nhật số dư đóng băng (frozen balance) cho Bidder khi tham gia đấu giá.

    public synchronized void updateFrozenBalance(String userId, double frozenBalance) {
        if (userId == null) return;
        List<User> users = readFromFile();
        boolean found = false;

        for (User u : users) {
            if (u.getId().equals(userId) && u instanceof Bidder bidder) {
                bidder.setFrozenBalance(frozenBalance);
                found = true;
                break;
            }
        }

        if (found) {
            writeToFile(users);
        }
    }

    // Xóa người dùng theo ID.

    public synchronized boolean deleteById(String id) {
        if (id == null) return false;
        List<User> users = readFromFile();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) {
            writeToFile(users);
        }
        return removed;
    }

    //Kiểm tra xem username đã tồn tại trong hệ thống chưa.

    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return readFromFile().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    // =========================================================================
    // CÁC HÀM XỬ LÝ SERIALIZATION FILE NHỊ PHÂN AN TOÀN ĐA LUỒNG
    // =========================================================================

    @SuppressWarnings("unchecked")
    private List<User> readFromFile() {
        lock.readLock().lock();
        try {
            if (!Files.exists(FILE_PATH) || Files.size(FILE_PATH) == 0) {
                return new ArrayList<>();
            }
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(FILE_PATH)))) {
                Object obj = ois.readObject();
                if (obj instanceof List<?> list) {
                    return new ArrayList<>((List<User>) list);
                }
            }
        } catch (EOFException e) {

            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file users.dat: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        return new ArrayList<>();
    }

    private void writeToFile(List<User> users) {
        lock.writeLock().lock();
        try {
            // Ghi ra file tạm .tmp trước rồi mới đổi tên để tránh hỏng file nếu mất điện/crash đột ngột
            Path tmpPath = FILE_PATH.resolveSibling("users.dat.tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmpPath)))) {
                oos.writeObject(new ArrayList<>(users));
                oos.flush();
            }
            Files.move(tmpPath, FILE_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi file users.dat: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}