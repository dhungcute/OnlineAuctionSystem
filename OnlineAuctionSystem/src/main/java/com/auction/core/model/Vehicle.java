package com.auction.core.model;
import java.time.LocalDateTime;

// LỚP ĐẠI DIỆN CHO MẶT HÀNG LÀ XE CỘ (VEHICLE)
public class Vehicle extends Item{
    private String brand;

    // Khởi tạo mặt hàng xe cộ mới với thương hiệu cụ thể.
    public Vehicle(String name,String description,double startingPrice,User owner,String brand){
        super(name,description,startingPrice,owner);
        this.brand=brand;
    }
    // Khởi tạo nhanh mặt hàng xe cộ mới, mặc định thương hiệu là "Unknown Brand".
    public Vehicle(String name,String description,double startignPrice,User owner){
        this(name,description,startingPrice,owner,"Unknown Brand");
    }
    // Tái tạo lại mặt hàng xe cộ từ dữ liệu cũ (khôi phục từ Database).
    public Vehicle(String id,LocalDateTime createdAt,String name,String description,double startingPrice,User owner,String brand){
        super(id,createdAt,name,description,startingPrice,owner);
        this.brand=brand;
    }
    public String getBrand(){
        return brand;
    }
    @Override
    public String getCategoryInfo(){
        return "Hãng: "+brand;
    }
    @Override
    public String getCategory(){
        return "Xe cộ";
    }
}
