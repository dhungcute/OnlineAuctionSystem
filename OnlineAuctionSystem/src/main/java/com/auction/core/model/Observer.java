package com.auction.core.model;
// Update giá realtime
public interface Observer {
    public void update(BidTransaction bidder);
}