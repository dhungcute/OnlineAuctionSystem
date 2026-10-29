package com.auction.core.model.auction;

import com.auction.core.model.item.Item;
import com.auction.core.model.user.Seller;

import java.util.ArrayList;
import java.util.List;

public class Auction {
    private String auctionId;
    private Item item;
    private Seller seller;
    private List<Bid> bids;
    private AuctionStatus status;

    public Auction(String auctionId, Item item, Seller seller) {
        this.auctionId = auctionId;
        this.item = item;
        this.seller = seller;
        this.bids = new ArrayList<>();
        this.status = AuctionStatus.ACTIVE;
    }

    // Logic kiểm tra và đặt giá
    public boolean placeBid(Bid bid) {
        if (status != AuctionStatus.ACTIVE) {
            System.out.println("Phiên đấu giá không ở trạng thái ACTIVE!");
            return false;
        }

        double currentHighest = getCurrentHighestPrice();
        if (bid.getAmount() <= currentHighest) {
            System.out.println("Mức giá phải lớn hơn giá hiện tại ($" + currentHighest + ")");
            return false;
        }

        bids.add(bid);
        System.out.println("Đặt giá thành công: $" + bid.getAmount() + " bởi " + bid.getBidder().getUsername());
        return true;
    }

    // Lấy giá cao nhất hiện tại (nếu chưa ai bid thì trả về giá khởi điểm của Item)
    public double getCurrentHighestPrice() {
        if (bids.isEmpty()) {
            return item.getStartingPrice();
        }
        return bids.get(bids.size() - 1).getAmount();
    }

    // Lấy lượt bid cao nhất
    public Bid getHighestBid() {
        if (bids.isEmpty()) return null;
        return bids.get(bids.size() - 1);
    }

    // Getters & Setters
    public String getAuctionId() { return auctionId; }
    public Item getItem() { return item; }
    public Seller getSeller() { return seller; }
    public List<Bid> getBids() { return bids; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
}