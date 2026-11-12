package com.auction.core.model;
import java.time.LocalDateTime;

// LỚP ĐẠI DIỆN CHO MẶT HÀNG ĐIỆN TỬ (ELECTRONICS).
public class Electronics extends Item{
    private int warrantyMonths;

// Khởi tạo mặt hàng điện tử mới với thời gian bảo hành cụ thể.
    public Electronics(String name,String description,double startingPrice,User owner,int warrantyMonths){
        super(name,description,startingPrice,owner);
        this.warrantyMonths=warrantyMonths;
    }
// Khởi tạo nhanh mặt hàng điện tử mới, mặc định thời gian bảo hành là 12 tháng.
    public Electronics(String name,String description,double startingPrice,User owner){
        this(name,description,startingPrice,owner,12);
    }
// Tái tạo lại mặt hàng điện tử từ dữ liệu cũ (khôi phục từ Database).
    public Electronics(String id,LocalDateTime createdAt,String name,String description,double startingPrice,User owner,int warrantyMonths){
        super(id,createdAt,name,description,startingPrice,owner);
        this.warrantyMonths=warrantyMonths;
    }
    public int getWarrantyMonths(){
        return warrantyMonths;
    }
    @Override
    public String getCategoryInfo(){
        return "Bảo hành: "+warrantyMonths+" tháng";
    }
    @Override
    public String getCategory(){
        return "Điện tử";
    }
}
