package com.auction.core.factory;

import com.auction.core.model.Item;
import com.auction.core.model.User;
import java.time.LocalDateTime;


public abstract class ItemFactory {
    protected abstract Item createItem(String name, String description, double startingPrice, User owner);
    protected abstract Item createItem(String id, LocalDateTime createAt, String name, String description, double startingPrice, User owner, Object attribute);
    protected abstract Item createItem(String name, String description, double startingPrice, User owner, Object atrribute);
}

