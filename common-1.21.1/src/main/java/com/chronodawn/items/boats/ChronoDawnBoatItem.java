package com.chronodawn.items.boats;

import com.chronodawn.entities.boats.ChronoDawnBoat;
import com.chronodawn.entities.boats.ChronoDawnBoatType;
import com.chronodawn.entities.boats.ChronoDawnChestBoat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

/**
 * Custom boat item for ChronoDawn mod boats.
 * Places ChronoDawnBoat or ChronoDawnChestBoat entities in water.
 *
 * Task: T268-T270 [US1] Create Time Wood Boat items
 */
public class ChronoDawnBoatItem extends Item {

    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    private final ChronoDawnBoatType boatType;
    private final boolean hasChest;

    public ChronoDawnBoatItem(ChronoDawnBoatType boatType, boolean hasChest, Properties properties) {
        super(properties.stacksTo(1));
        this.boatType = boatType;
        this.hasChest = hasChest;
    }

    /**
     * Creates properties for boat items.
     */
    public static Properties createProperties() {
        return new Properties().stacksTo(1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Raycast to find water surface
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }

        Vec3 viewVector = player.getViewVector(1.0F);
        double range = 5.0D;
        List<Entity> entities = level.getEntities(
                player,
                player.getBoundingBox().expandTowards(viewVector.scale(range)).inflate(1.0D),
                ENTITY_PREDICATE
        );

        if (!entities.isEmpty()) {
            Vec3 eyePosition = player.getEyePosition();
            for (Entity entity : entities) {
                AABB boundingBox = entity.getBoundingBox().inflate((double) entity.getPickRadius());
                if (boundingBox.contains(eyePosition)) {
                    return InteractionResultHolder.pass(itemStack);
                }
            }
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            Boat boat = createBoat(level, hitResult);
            boat.setYRot(player.getYRot());

            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResultHolder.fail(itemStack);
            }

            if (!level.isClientSide()) {
                level.addFreshEntity(boat);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());

                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }

        return InteractionResultHolder.pass(itemStack);
    }

    /**
     * Creates the appropriate boat entity based on the item configuration.
     */
    private Boat createBoat(Level level, HitResult hitResult) {
        Vec3 location = hitResult.getLocation();

        if (hasChest) {
            ChronoDawnChestBoat chestBoat = new ChronoDawnChestBoat(level, location.x, location.y, location.z);
            chestBoat.setChronoDawnBoatType(boatType);
            return chestBoat;
        } else {
            ChronoDawnBoat boat = new ChronoDawnBoat(level, location.x, location.y, location.z);
            boat.setChronoDawnBoatType(boatType);
            return boat;
        }
    }

    /**
     * Returns the boat type for this item.
     */
    public ChronoDawnBoatType getBoatType() {
        return boatType;
    }

    /**
     * Returns whether this is a chest boat item.
     */
    public boolean hasChest() {
        return hasChest;
    }
}
