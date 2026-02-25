package com.chronodawn.entities.boats;

import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Enum defining the different types of ChronoDawn boats.
 * Each type corresponds to a Time Wood variant.
 *
 * Task: T268-T270 [US1] Create Time Wood Boat variants
 */
public enum ChronoDawnBoatType {
    TIME_WOOD("time_wood", () -> ModBlocks.TIME_WOOD_PLANKS.get(), () -> ModItems.TIME_WOOD_BOAT.get(), () -> ModItems.TIME_WOOD_CHEST_BOAT.get()),
    DARK_TIME_WOOD("dark_time_wood", () -> ModBlocks.DARK_TIME_WOOD_PLANKS.get(), () -> ModItems.DARK_TIME_WOOD_BOAT.get(), () -> ModItems.DARK_TIME_WOOD_CHEST_BOAT.get()),
    ANCIENT_TIME_WOOD("ancient_time_wood", () -> ModBlocks.ANCIENT_TIME_WOOD_PLANKS.get(), () -> ModItems.ANCIENT_TIME_WOOD_BOAT.get(), () -> ModItems.ANCIENT_TIME_WOOD_CHEST_BOAT.get());

    private final String name;
    private final Supplier<Block> planks;
    private final Supplier<Item> boatItem;
    private final Supplier<Item> chestBoatItem;

    ChronoDawnBoatType(String name, Supplier<Block> planks, Supplier<Item> boatItem, Supplier<Item> chestBoatItem) {
        this.name = name;
        this.planks = planks;
        this.boatItem = boatItem;
        this.chestBoatItem = chestBoatItem;
    }

    public String getName() {
        return name;
    }

    public Block getPlanks() {
        return planks.get();
    }

    public Item getBoatItem() {
        return boatItem.get();
    }

    public Item getChestBoatItem() {
        return chestBoatItem.get();
    }

    public static ChronoDawnBoatType byName(String name) {
        for (ChronoDawnBoatType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return TIME_WOOD;
    }

    public static ChronoDawnBoatType byId(int id) {
        if (id >= 0 && id < values().length) {
            return values()[id];
        }
        return TIME_WOOD;
    }
}
