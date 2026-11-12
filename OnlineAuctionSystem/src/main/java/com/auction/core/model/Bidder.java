package com.auction.core.model;

import java.time.LocalDateTime;
// LỚP ĐẠI DIỆN CHO TÀI KHOẢN NGƯỜI ĐẤU GIÁ (BIDDER), QUẢN LÝ SỐ DƯ TÀI KHOẢN VÀ TIỀN ĐÓNG BĂNG KHI ĐẶT CỌC/ĐẶT GIÁ.
public class Bidder extends User {
    private double accountBalance;
    private double frozenBalance;
    
// Khởi tạo 1 người đấu giá mới với số dư ban đầu, mặc định tiền đóng băng bằng 0.
    public Bidder(String username, String password, double accountBalance) {
        super(username, password);
        this.accountBalance = accountBalance;
        this.frozenBalance  = 0.0;
    }
// Khởi tạo người đấu giá từ dữ liệu cũ với số dư xác định, tiền đóng băng bằng 0.
    public Bidder(String id, LocalDateTime createdAt, String username, String password, double accountBalance) {
        super(id, createdAt, username, password);
        this.accountBalance = accountBalance;
        this.frozenBalance  = 0.0;
    }
// Khởi tạo người đấu giá từ dữ liệu cũ, khôi phục cả số dư tài khoản và số tiền đang bị đóng băng.
    public Bidder(String id, LocalDateTime createdAt, String username, String password,
                  double accountBalance, double frozenBalance) {
        super(id, createdAt, username, password);
        this.accountBalance = accountBalance;
        this.frozenBalance  = Math.max(0.0, frozenBalance);
    }
    @Override
    public String getRole() { return "Bidder"; }

    public double getAccountBalance() { return accountBalance; }
    public double getFrozenBalance() { return frozenBalance; }

    public double getAvailableBalance() {
        return Math.max(0.0, accountBalance - frozenBalance);
    }
    public void setAccountBalance(double accountBalance) { this.accountBalance = accountBalance; }

    public void setFrozenBalance(double frozenBalance) {
        this.frozenBalance = Math.max(0.0, frozenBalance);
    }
    public void AddBalance(double amount) {
        if (amount > 0) accountBalance += amount;
    }
    public void addBalance(double amount) { AddBalance(amount); }
    
// Đóng băng một khoản tiền (cọc) khi đặt giá.
    public synchronized boolean freezeFunds(double amount) {
        if (amount <= 0) return false;
        if (getAvailableBalance() < amount) return false;
        this.frozenBalance += amount;
        return true;
    }
// Hủy đóng băng/Hoàn trả lại số tiền cọc khi người đấu giá bị người khác vượt giá.
    public synchronized void unfreezeFunds(double amount) {
        if (amount <= 0) return;
        this.frozenBalance = Math.max(0.0, this.frozenBalance - amount);
    }
// Trừ thẳng tiền khỏi tài khoản khi thắng đấu giá, đồng thời gỡ đóng băng khoản đó.
    public synchronized boolean deductBalance(double amount) {
        if (amount > 0 && this.accountBalance >= amount) {
            this.accountBalance -= amount;
            this.frozenBalance = Math.max(0.0, this.frozenBalance - amount);
            return true;
        }
        return false;
    }
}
