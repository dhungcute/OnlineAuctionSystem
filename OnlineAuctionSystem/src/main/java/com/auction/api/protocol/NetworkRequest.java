package com.auction.api.protocol;

import java.io.Serializable;

/**
 * Gói tin tiêu chuẩn Client gửi lên Server qua Socket
 */
public class NetworkRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action; // Hành động: LOGIN, REGISTER, GET_ALL_AUCTIONS, PLACE_BID, CREATE_AUCTION...
    private Object data;   // Dữ liệu truyền kèm (User, Auction, mảng tham số...)

    public NetworkRequest(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() { return action; }
    public Object getData() { return data; }
}
