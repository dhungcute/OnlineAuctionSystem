package com.auction.dao;

import com.auction.core.model.Auction;
import java.util.List;
import java.util.Optional;

public class FileAuctionDao {
    //Class thực hiện CRUD phiên đấu giá
    private static final String FILE_NAME = "auctions.dat";
    private List<Auction> memoryAuctions;

    public FileAuctionDao() {
        this.memoryAuctions = DataStorageManager.loadFromFile(FILE_NAME);
    }

    public synchronized List<Auction> getAll() {
        return memoryAuctions;
    }

    public synchronized Optional<Auction> findById(String id) {
        return memoryAuctions.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public synchronized void save(Auction auction) {
        memoryAuctions.add(auction);
        DataStorageManager.saveToFile(FILE_NAME, memoryAuctions); 
    }

    public synchronized void update(Auction auction) {
        for (int i = 0; i < memoryAuctions.size(); i++) {
            if (memoryAuctions.get(i).getId().equals(auction.getId())) {
                memoryAuctions.set(i, auction);
                break;
            }
        }
        DataStorageManager.saveToFile(FILE_NAME, memoryAuctions); 
    }
}