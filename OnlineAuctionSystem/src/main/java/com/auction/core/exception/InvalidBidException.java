package com.auction.core.exception;

// Ngoại lệ nghiệp vụ: Ném ra khi giá đặt không hợp lệ (thấp hơn giá sàn, sai bước giá, không đủ tiền cọc)
public class InvalidBidException extends Exception{
    public InvalidBidException(String message){
        super(message);
    }
}
