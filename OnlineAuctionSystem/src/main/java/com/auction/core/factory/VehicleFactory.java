package com.auction.core.factory;

import com.auction.core.model.Item;
import com.auction.core.model.User;
import com.auction.core.model.Vehicle;
import java.time.LocalDateTime;

public class VehicleFactory extends ItemFactory {
    @Override 
    protected Item createItem(String name, String description, double startingPrice, User owner) {
        return new Vehicle(name, description, startingPrice, owner);
    } 
    @Override 
    protected Item createItem(String name, String description, double startingPrice, User owner, Object attribute) {
        String brand = (String)attribute;
        return new Vehicle(name, description, startingPrice, owner, brand);
    }
    @Override 
    protected Item createItem(String id, LocalDateTime createAt, String name, String description, double startingPrice, User owner, Object attribute) {
        String brand = (String)attribute;
        return new Vehicle(id, createAt, name, description, startingPrice, owner, brand);
    }
}
