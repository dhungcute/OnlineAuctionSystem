package com.auction.core.factory;

import com.auction.core.model.Item;
import com.auction.core.model.User;
import com.auction.core.model.Electronics;
import java.time.LocalDateTime;


public class ElectronicsFactory extends ItemFactory {
    @Override 
    protected Item createItem(String name, String description, double startingPrice, User owner) {
        return new Electronics(name, description, startingPrice, owner);
    } 
    @Override 
    protected Item createItem(String name, String description, double startingPrice, User owner, Object attribute) {
        int warrantyMonths = (int)attribute;
        return new Electronics(name, description, startingPrice, owner, warrantyMonths);
    }
    @Override 
    protected Item createItem(String id, LocalDateTime createAt, String name, String description, double startingPrice, User owner, Object attribute) {
        int warrantyMonths = (int)attribute;
        return new Electronics(id, createAt, name, description, startingPrice, owner, warrantyMonths);
    }
}
