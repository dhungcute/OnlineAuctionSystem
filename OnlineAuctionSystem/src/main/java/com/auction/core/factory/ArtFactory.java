package com.auction.core.factory;

import com.auction.core.model.Item;
import com.auction.core.model.User;
import com.auction.core.model.Art;
import java.time.LocalDateTime;

public class ArtFactory extends ItemFactory {
    @Override 
    protected Item createItem(String name, String description, double startingPrice, User owner) {
        return new Art(name, description, startingPrice, owner);
    }
    @Override 
    protected Item createItem(String name, String description, double startingPrice, User owner, Object attribute) {
        String artist = (String)attribute;
        return new Art(name, description, startingPrice, owner, artist);
    }
    @Override 
    protected Item createItem(String id, LocalDateTime createAt, String name, String description, double startingPrice, User owner, Object attribute) {
        String artist = (String)attribute;
        return new Art(id, createAt, name, description, startingPrice, owner, artist);
    }  
}
