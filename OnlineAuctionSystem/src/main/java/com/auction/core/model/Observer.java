package com.auction.core.model;

public interface Observer {
    void update(BidTransaction bidder);
}