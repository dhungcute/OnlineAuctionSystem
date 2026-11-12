package com.auction.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorageManager {
    //Class đọc/ghi file bằng tuần tự hóa Serializable
    private static final String DATA_DIR = "data/";

    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static synchronized <T extends Serializable> void saveToFile(String fileName, List<T> dataList) {
        String filePath = DATA_DIR + fileName;
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(dataList);
            System.out.println("[Storage] Đã lưu " + dataList.size() + " bản ghi vào " + filePath);
        } catch (IOException e) {
            System.err.println("[Storage] Lỗi khi lưu file " + filePath + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends Serializable> List<T> loadFromFile(String fileName) {
        String filePath = DATA_DIR + fileName;
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Storage] Lỗi khi đọc file " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}