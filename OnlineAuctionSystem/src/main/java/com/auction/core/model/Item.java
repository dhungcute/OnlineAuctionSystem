package com.auction.core.model;
import java.time.LocalDateTime;

// LỚP TRỪU TƯỢNG (ABSTRACT) ĐẠI DIỆN CHO MẶT HÀNG ĐẤU GIÁ CƠ BẢN.
public abstract class Item extends Entity{
    private String name;
    private String description;
    private double startingPrice;
    private User owner;
    private String imageUrl;

// Khởi tạo một mặt hàng mới trên hệ thống.
    public Item(String name,String description,double startingPrice,User owner) {
        super();
        this.name=name;
        this.description=description;
        this.startingPrice=startingPrice;
        this.owner=owner;
        this.imageUrl="";
    }
// Tái tạo lại mặt hàng từ dữ liệu đã có sẵn (khôi phục thông tin từ Database).
    public Item(String id,LocalDateTime createdAt,String name,String description,double startingPrice,User owner){
        super(id,createdAt);
        this.name=name;
        this.description=description;
        this.startingPrice=startingPrice;
        this.owner=owner;
        this.imageUrl="";
    }
    public abstract String getCategoryInfo();
    public String getCategory(){
        return getCategoryInfo();
    }
    public String getName(){
        return name;
    }
    public double getStartingPrice(){
        return startingPrice;
    }
    public String getDescription() {
        return description;
    }
    public User getOwner(){
        return owner;
    }
    public String getImageUrl(){
        return imageUrl!=null ? imageUrl : "";
    }
    public double getBasePrice(){
        return startingPrice;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setDescription(String description){
        this.description=description;
    }
    public void setStartingPrice(double price){
        this.startingPrice=price;
    }
    public void setImageUrl(String imageUrl){
        this.imageUrl=imageUrl;
    }
}
