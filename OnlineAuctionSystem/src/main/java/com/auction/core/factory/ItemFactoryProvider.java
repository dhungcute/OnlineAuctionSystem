package com.auction.core.factory;

import java.util.Map;
import java.util.EnumMap;

public class ItemFactoryProvider {
    public static final Map<ItemCategory, ItemFactory> FACTORIES = new EnumMap<>(ItemCategory.class);

    static {
        FACTORIES.put(ItemCategory.ELECTRONICS, new ElectronicsFactory());
        FACTORIES.put(ItemCategory.ART, new ArtFactory());
        FACTORIES.put(ItemCategory.VEHICLE, new VehicleFactory());
    }

    public static ItemFactory getFactory(ItemCategory category) {
        ItemFactory factory = FACTORIES.get(category);
        if (factory == null) throw new IllegalArgumentException("Unsupported item category");
        return factory;
    }
}
