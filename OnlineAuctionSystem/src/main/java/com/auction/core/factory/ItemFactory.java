package com.auction.core.factory;

import com.auction.core.model.*;

public abstract class ItemFactory {
	// TẠO VẬT PHẨM MỚI 
	public abstract Item createItem(
        String name,
        String description,
        double price,
        User owner
    );
	// KHÔI PHỤC VẬT PHẨM TỪ CƠ SỞ DỮ LIỆU 
	public abstract Item createItem(
        String id,
        java.time.LocalDateTime createdAt,
        String name,
        String description,
        double price,
        User owner
    );
}
