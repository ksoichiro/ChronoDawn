package com.chronodawn.items.equipment;

import com.chronodawn.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Temporal Amber Armor Item - Tier 2 armor pieces from Temporal Amber.
 *
 * Tier 2 armor crafted from Temporal Amber.
 * Provides diamond-comparable protection with enhanced durability.
 *
 * Armor Set:
 * - Helmet: Defense 3, Durability 385
 * - Chestplate: Defense 7, Durability 560
 * - Leggings: Defense 6, Durability 525
 * - Boots: Defense 3, Durability 455
 *
 * Total Set Defense: 19 (iron: 15, diamond: 20)
 * Toughness: 1.5f
 * Enchantability: 10
 *
 * Repair Material: Temporal Amber Dust
 *
 * Reference: T302 - Temporal Amber Armor Items
 */
public class TemporalAmberArmorItem extends ArmorItem {
    private static final int REPAIR_INTERVAL_TICKS = 60;
    private static final int COMBAT_COOLDOWN_TICKS = 200;
    private static final int REPAIR_AMOUNT_PER_PIECE = 5;

    public TemporalAmberArmorItem(ArmorType type, Properties properties) {
        // In 1.21.4, ArmorItem constructor expects ArmorMaterial value, not Holder<ArmorMaterial>
        super(TemporalAmberArmorMaterial.TEMPORAL_AMBER.value(), type, properties);
    }

    /**
     * Create default properties for Temporal Amber Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(ArmorType type) {
        return new Properties()
                .stacksTo(1)
                .durability(type.getDurability(TemporalAmberArmorMaterial.BASE_DURABILITY));
    }

    /**
     * Check if player is wearing full Temporal Amber armor set.
     *
     * @param player The player to check
     * @return true if player is wearing full Temporal Amber armor set
     */
    public static boolean isWearingFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof TemporalAmberArmorItem;
    }

    /**
     * Auto-repair entry point for pre-1.21.5 versions.
     *
     * Pre-1.21.5 Item.inventoryTick is not called for equipped armor slots, so the
     * 1.21.5+ pattern of overriding inventoryTick does not work here. Instead, this
     * method is invoked once per player tick from EntityEventHandler's PLAYER_POST hook.
     */
    public static void tryAutoRepair(Player player) {
        if (!isWearingFullSet(player)) return;
        // Use player.tickCount (entity-local) for both timers — getLastHurtByMobTimestamp()
        // is assigned from tickCount, so diffing against gameTime would be meaningless.
        if (player.tickCount % REPAIR_INTERVAL_TICKS != 0) return;
        if (player.tickCount - player.getLastHurtByMobTimestamp() < COMBAT_COOLDOWN_TICKS) return;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        boolean anyDamaged = head.getDamageValue() > 0 || chest.getDamageValue() > 0
                || legs.getDamageValue() > 0 || feet.getDamageValue() > 0;
        if (!anyDamaged) return;

        int dustSlot = findDustSlot(player);
        if (dustSlot == -1) return;

        player.getInventory().getItem(dustSlot).shrink(1);
        repairPiece(head);
        repairPiece(chest);
        repairPiece(legs);
        repairPiece(feet);
    }

    private static int findDustSlot(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item.is(ModItems.TEMPORAL_AMBER_DUST.get())) {
                return i;
            }
        }
        return -1;
    }

    private static void repairPiece(ItemStack stack) {
        int currentDamage = stack.getDamageValue();
        if (currentDamage > 0) {
            stack.setDamageValue(Math.max(0, currentDamage - REPAIR_AMOUNT_PER_PIECE));
        }
    }
}
