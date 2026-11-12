package com.auction.core.exception;

// Ngoại lệ nghiệp vụ: Ném ra khi vi phạm vòng đời/trạng thái phiên đấu giá (đặt giá khi phiên đóng, hủy phiên đã kết thúc...)
public class InvalidStatusException extends Exception{
    public InvalidStatusException(String message){
        super(message);
    }
}
