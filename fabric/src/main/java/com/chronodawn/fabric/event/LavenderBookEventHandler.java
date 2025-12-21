package com.chronodawn.fabric.event;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModDimensions;
import dev.architectury.event.events.common.TickEvent;
import io.wispforest.lavender.book.Book;
import io.wispforest.lavender.book.BookLoader;
import io.wispforest.lavender.book.LavenderBookItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fabric-specific event handler for Lavender guidebook distribution.
 *
 * This handler automatically gives the Chrono Dawn Chronicle guidebook to players
 * when they enter the Chrono Dawn dimension for the first time.
 *
 * Note: This is Fabric-only because Lavender is currently only available for Fabric.
 */
public class LavenderBookEventHandler {
    // Track player dimensions to detect changes
    private static final Map<UUID, ResourceKey<Level>> playerDimensions = new HashMap<>();

    /**
     * Register Lavender book event listeners (Fabric only).
     */
    public static void register() {
        // Register server tick event to monitor player dimension changes
        TickEvent.SERVER_POST.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ResourceKey<Level> currentDimension = player.level().dimension();
                ResourceKey<Level> previousDimension = playerDimensions.get(player.getUUID());

                // Check if dimension changed
                if (previousDimension != null && !previousDimension.equals(currentDimension)) {
                    // Player changed dimension
                    if (currentDimension.equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
                        // Player entered ChronoDawn - give guidebook
                        giveChronicleBook(player);
                    }
                }

                // Update tracked dimension
                playerDimensions.put(player.getUUID(), currentDimension);
            }
        });

        ChronoDawn.LOGGER.info("Registered LavenderBookEventHandler (Fabric)");
    }

    /**
     * Give Chrono Dawn Chronicle guidebook to player if they don't already have it.
     * Uses Lavender's dynamic book system.
     *
     * @param player Player to give the book to
     */
    private static void giveChronicleBook(ServerPlayer player) {
        // Check if player already has the chronicle book
        if (hasChronicleBook(player)) {
            ChronoDawn.LOGGER.info("Player {} already has Chronicle book, skipping",
                player.getName().getString());
            return;
        }

        try {
            // Get the chronicle book definition
            ResourceLocation bookId = ResourceLocation.fromNamespaceAndPath("chronodawn", "chronicle");
            Book book = BookLoader.get(bookId);

            if (book == null) {
                ChronoDawn.LOGGER.error("Failed to find Chronicle book with ID: {}", bookId);
                return;
            }

            // Create dynamic book item stack
            ItemStack bookStack = LavenderBookItem.createDynamic(book);

            // Give book to player
            if (!player.getInventory().add(bookStack)) {
                // Inventory full, drop at player's feet
                player.drop(bookStack, false);
                ChronoDawn.LOGGER.info("Player {} inventory full, dropped Chronicle book at their position",
                    player.getName().getString());
            } else {
                ChronoDawn.LOGGER.info("Gave Chronicle book to player {}", player.getName().getString());
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to give Chronicle book to player {}: {}",
                player.getName().getString(), e.getMessage(), e);
        }
    }

    /**
     * Check if player already has the Chronicle book in their inventory.
     *
     * @param player Player to check
     * @return true if player has the book
     */
    private static boolean hasChronicleBook(ServerPlayer player) {
        ResourceLocation bookId = ResourceLocation.fromNamespaceAndPath("chronodawn", "chronicle");

        // Check all inventory slots
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof LavenderBookItem) {
                // Check if this is our chronicle book
                ResourceLocation stackBookId = stack.get(LavenderBookItem.BOOK_ID);
                if (bookId.equals(stackBookId)) {
                    return true;
                }
            }
        }

        return false;
    }
}
