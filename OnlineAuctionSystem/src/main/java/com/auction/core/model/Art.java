package com.auction.core.model;
import java.time.LocalDateTime;

// LỚP ĐẠI DIỆN CHO MẶT HÀNG NGHỆ THUẬT (ART).
public class Art extends Item{
    private String artistName;
    // Khởi tạo 1 tác phẩm nghệ thuật mới với đầy đủ thông tin, bao gồm cả tên họa sĩ.
    public Art(String name,String description,double startingPrice,User owner,String artistName){
        super(name,description,startingPrice,owner);
        this.artistName=artistName;
    }
    // Khởi tạo một tác phẩm nghệ thuật mới khi không rõ tên họa sĩ (mặc định là "Unknown").
    public Art(String name,String description,double startingPrice,User owner){
        this(name,description,startingPrice,owner,"Unknown");
    }
    // Khởi tạo tác phẩm nghệ thuật từ dữ liệu đã có sẵn (lấy từ Database).
    public Art(String id,LocalDateTime createdAt,String name,String description,double startingPrice,User owner,String artistName){
        super(id,createdAt,name,description,startingPrice,owner);
        this.artistName=artistName;
    }
    @Override
    public String getCategoryInfo(){
        return "Họa sĩ: "+artistName;
    }
    @Override
    public String getCategory(){
        return "Nghệ thuật";
    }
    public String getArtistName(){
        return artistName;
    }
}
