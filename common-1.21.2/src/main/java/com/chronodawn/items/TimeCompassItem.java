package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatHandlers;
import com.chronodawn.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Optional;

/**
 * Time Compass - Points to nearest key structures in ChronoDawn.
 *
 * This compass helps players locate important structures:
 * - Ancient Ruins (Overworld)
 * - Desert Clock Tower (ChronoDawn)
 * - Master Clock Tower (ChronoDawn)
 *
 * Structure Type Storage:
 * - Stored in NBT custom data component
 * - Key: "TargetStructure"
 * - Values: "ancient_ruins", "desert_clock_tower", "master_clock_tower"
 *
 * Obtaining:
 * - Trade with Time Keeper (each type sold separately)
 * - Different compass for each structure type
 *
 * Behavior:
 * - Compass needle points to nearest structure of specified type
 * - Only works in appropriate dimension (Ancient Ruins in Overworld, others in ChronoDawn)
 * - Shows tooltip indicating target structure type
 *
 * Future Enhancement (T115k Phase 2):
 * - Right-click to cycle through structure types
 * - Single compass for all structures
 *
 * Task: T115k [US2] Create Time Compass item
 * Task: T115h [US2] Add to Time Keeper trades
 */
public class TimeCompassItem extends Item {
    // NBT keys for structure targeting
    public static final String NBT_TARGET_STRUCTURE = "TargetStructure";
    public static final String NBT_TARGET_POS_X = "TargetX";
    public static final String NBT_TARGET_POS_Y = "TargetY";
    public static final String NBT_TARGET_POS_Z = "TargetZ";
    public static final String NBT_TARGET_DIMENSION = "TargetDimension";

    // Structure type constants
    public static final String STRUCTURE_ANCIENT_RUINS = "ancient_ruins";
    public static final String STRUCTURE_DESERT_CLOCK_TOWER = "desert_clock_tower";
    public static final String STRUCTURE_MASTER_CLOCK = "master_clock";
    public static final String STRUCTURE_PHANTOM_CATACOMBS = "phantom_catacombs";
    public static final String STRUCTURE_GUARDIAN_VAULT = "guardian_vault";
    public static final String STRUCTURE_CLOCKWORK_DEPTHS = "clockwork_depths";
    public static final String STRUCTURE_ENTROPY_CRYPT = "entropy_crypt";

    public TimeCompassItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Item.Properties()
            .stacksTo(1) // Compass doesn't stack
            .setId(ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_compass")));
    }

    /**
     * Create a Time Compass targeting a specific structure.
     *
     * @param structureType Structure type constant (STRUCTURE_ANCIENT_RUINS, etc.)
     * @return ItemStack with target structure set in NBT
     */
    public static ItemStack createCompass(String structureType) {
        ItemStack stack = new ItemStack(com.chronodawn.registry.ModItems.TIME_COMPASS.get());

        // Store structure type in custom data (version-independent)
        CompatHandlers.ITEM_DATA.setString(stack, NBT_TARGET_STRUCTURE, structureType);

        return stack;
    }

    /**
     * Get the target structure type from an ItemStack.
     *
     * @param stack Time Compass ItemStack
     * @return Structure type string, or null if not set
     */
    public static String getTargetStructure(ItemStack stack) {
        String target = CompatHandlers.ITEM_DATA.getString(stack, NBT_TARGET_STRUCTURE);
        return target.isEmpty() ? null : target;
    }

    /**
     * Set the target position for the compass.
     *
     * @param stack Time Compass ItemStack
     * @param globalPos Target position (dimension + coordinates)
     */
    public static void setTargetPosition(ItemStack stack, GlobalPos globalPos) {
        CompatHandlers.ITEM_DATA.updateCustomData(stack, tag -> {
            tag.putInt(NBT_TARGET_POS_X, globalPos.pos().getX());
            tag.putInt(NBT_TARGET_POS_Y, globalPos.pos().getY());
            tag.putInt(NBT_TARGET_POS_Z, globalPos.pos().getZ());
            tag.putString(NBT_TARGET_DIMENSION, globalPos.dimension().location().toString());
        });
    }

    /**
     * Get the target position from the compass.
     *
     * @param stack Time Compass ItemStack
     * @return Target position, or empty if not set
     */
    public static Optional<GlobalPos> getTargetPosition(ItemStack stack) {
        CompoundTag tag = CompatHandlers.ITEM_DATA.getCustomData(stack);
        if (tag.contains(NBT_TARGET_POS_X) && tag.contains(NBT_TARGET_DIMENSION)) {
            int x = tag.getInt(NBT_TARGET_POS_X);
            int y = tag.getInt(NBT_TARGET_POS_Y);
            int z = tag.getInt(NBT_TARGET_POS_Z);
            String dimensionStr = tag.getString(NBT_TARGET_DIMENSION);

            try {
                ResourceKey<Level> dimension = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.parse(dimensionStr)
                );
                return Optional.of(GlobalPos.of(dimension, new BlockPos(x, y, z)));
            } catch (Exception e) {
                // Invalid dimension string
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Get localized name for structure type.
     *
     * @param structureType Structure type constant
     * @return Localized structure name
     */
    private static String getStructureDisplayName(String structureType) {
        return switch (structureType) {
            case STRUCTURE_ANCIENT_RUINS -> "item.chronodawn.time_compass.target.ancient_ruins";
            case STRUCTURE_DESERT_CLOCK_TOWER -> "item.chronodawn.time_compass.target.desert_clock_tower";
            case STRUCTURE_MASTER_CLOCK -> "item.chronodawn.time_compass.target.master_clock";
            case STRUCTURE_PHANTOM_CATACOMBS -> "item.chronodawn.time_compass.target.phantom_catacombs";
            case STRUCTURE_GUARDIAN_VAULT -> "item.chronodawn.time_compass.target.guardian_vault";
            case STRUCTURE_CLOCKWORK_DEPTHS -> "item.chronodawn.time_compass.target.clockwork_depths";
            case STRUCTURE_ENTROPY_CRYPT -> "item.chronodawn.time_compass.target.entropy_crypt";
            default -> "item.chronodawn.time_compass.target.unknown";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String targetStructure = getTargetStructure(stack);

        if (targetStructure != null) {
            // Add target structure type
            tooltip.add(Component.translatable("item.chronodawn.time_compass.tooltip.target")
                .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("  ")
                .append(Component.translatable(getStructureDisplayName(targetStructure)))
                .withStyle(ChatFormatting.AQUA));

            // Add coordinates if available
            Optional<GlobalPos> targetPos = getTargetPosition(stack);
            if (targetPos.isPresent()) {
                BlockPos pos = targetPos.get().pos();
                tooltip.add(Component.literal("  ")
                    .append(Component.literal(String.format("(%d, %d)", pos.getX(), pos.getZ())))
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            // No target set (shouldn't happen in normal gameplay)
            tooltip.add(Component.translatable("item.chronodawn.time_compass.tooltip.no_target")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Enchantment glint effect for visual appeal
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Only process on server side
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            String targetStructure = getTargetStructure(stack);

            if (targetStructure == null) {
                // No target structure set (shouldn't happen in normal gameplay)
                player.displayClientMessage(
                    Component.translatable("item.chronodawn.time_compass.no_target")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                // In 1.21.2, InteractionResult.FAIL doesn't take ItemStack
                return InteractionResult.FAIL;
            }

            // Check if already has position
            Optional<GlobalPos> existingPos = getTargetPosition(stack);
            if (existingPos.isPresent()) {
                // Already located - show current target and player position
                BlockPos targetPos = existingPos.get().pos();
                BlockPos playerPos = player.blockPosition();

                // Calculate distance and direction
                int dx = targetPos.getX() - playerPos.getX();
                int dz = targetPos.getZ() - playerPos.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                String direction = getDirection(dx, dz);

                Component structureName = Component.translatable(getStructureDisplayName(targetStructure));
                player.displayClientMessage(
                    Component.translatable("item.chronodawn.time_compass.current_location",
                        structureName,
                        targetPos.getX(), targetPos.getZ(),
                        playerPos.getX(), playerPos.getZ(),
                        (int)distance,
                        Component.translatable("direction." + direction))
                        .withStyle(ChatFormatting.AQUA),
                    false
                );
                level.playSound(null, player.blockPosition(), ModSounds.TIME_COMPASS_CHIME.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }

            // Search for structure
            ServerLevel serverLevel = (ServerLevel) level;
            boolean success = locateAndSetStructure(serverLevel, stack, targetStructure, serverPlayer);

            if (success) {
                Optional<GlobalPos> newPos = getTargetPosition(stack);
                if (newPos.isPresent()) {
                    BlockPos targetPos = newPos.get().pos();
                    BlockPos playerPos = player.blockPosition();

                    // Calculate distance and direction
                    int dx = targetPos.getX() - playerPos.getX();
                    int dz = targetPos.getZ() - playerPos.getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    String direction = getDirection(dx, dz);

                    Component structureName = Component.translatable(getStructureDisplayName(targetStructure));
                    player.displayClientMessage(
                        Component.translatable("item.chronodawn.time_compass.located",
                            structureName,
                            targetPos.getX(), targetPos.getZ(),
                            (int)distance,
                            Component.translatable("direction." + direction))
                            .withStyle(ChatFormatting.GREEN),
                        false
                    );
                    level.playSound(null, player.blockPosition(), ModSounds.TIME_COMPASS_UPDATE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResult.SUCCESS;
            } else {
                Component structureName = Component.translatable(getStructureDisplayName(targetStructure));
                player.displayClientMessage(
                    Component.translatable("item.chronodawn.time_compass.not_found",
                        structureName)
                        .withStyle(ChatFormatting.YELLOW),
                    false
                );
                level.playSound(null, player.blockPosition(), ModSounds.TIME_COMPASS_BREAK.get(), SoundSource.PLAYERS, 1.0f, 0.5f);
                return InteractionResult.FAIL;
            }
        }

        // In 1.21.2, return success on client side
        return InteractionResult.SUCCESS;
    }

    /**
     * Locate a structure and set its position in the compass.
     *
     * @param serverLevel Server level to search in
     * @param stack Time Compass ItemStack
     * @param structureType Structure type to locate
     * @param player Player using the compass
     * @return true if structure was found and position set
     */
    private static boolean locateAndSetStructure(ServerLevel serverLevel, ItemStack stack, String structureType, ServerPlayer player) {
        // Determine which dimension to search in
        ServerLevel searchLevel = serverLevel;
        ResourceLocation structureId;

        switch (structureType) {
            case STRUCTURE_ANCIENT_RUINS:
                // Ancient Ruins are in the Overworld
                searchLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
                structureId = CompatResourceLocation.create("chronodawn", "ancient_ruins");
                break;
            case STRUCTURE_DESERT_CLOCK_TOWER:
                // Desert Clock Tower is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "desert_clock_tower");
                break;
            case STRUCTURE_MASTER_CLOCK:
                // Master Clock is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "master_clock");
                break;
            case STRUCTURE_PHANTOM_CATACOMBS:
                // Phantom Catacombs is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "phantom_catacombs");
                break;
            case STRUCTURE_GUARDIAN_VAULT:
                // Guardian Vault is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "guardian_vault");
                break;
            case STRUCTURE_CLOCKWORK_DEPTHS:
                // Clockwork Depths is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "clockwork_depths");
                break;
            case STRUCTURE_ENTROPY_CRYPT:
                // Entropy Crypt is in ChronoDawn dimension
                searchLevel = serverLevel.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION, CompatResourceLocation.create("chronodawn", "chronodawn"))
                );
                structureId = CompatResourceLocation.create("chronodawn", "entropy_crypt");
                break;
            default:
                return false;
        }

        if (searchLevel == null) {
            return false;
        }

        // Get player's position as search origin
        BlockPos searchOrigin = player.blockPosition();

        // Get structure registry
        var structureRegistry = searchLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        // 1.21.2: get() now returns Optional<Reference<Structure>>
        var structureHolderOptional = structureRegistry.get(structureId);

        if (structureHolderOptional.isPresent()) {
            // Create HolderSet for the single structure
            // 1.21.2: wrapAsHolder() is no longer needed, get() already returns Reference<Structure>
            HolderSet<Structure> structureSet = HolderSet.direct(structureHolderOptional.get());

            // Locate nearest structure
            var structurePair = searchLevel.getChunkSource().getGenerator().findNearestMapStructure(
                searchLevel,
                structureSet,
                searchOrigin,
                100, // Search radius in chunks
                false // Skip known structures
            );

            if (structurePair != null) {
                BlockPos structurePos = structurePair.getFirst();
                GlobalPos globalPos = GlobalPos.of(searchLevel.dimension(), structurePos);
                setTargetPosition(stack, globalPos);
                return true;
            }
        }

        return false;
    }

    /**
     * Get direction string from dx and dz.
     * Returns one of 8 cardinal/ordinal directions.
     *
     * @param dx Delta X (target - player)
     * @param dz Delta Z (target - player)
     * @return Direction key (e.g., "north", "northeast", "east", etc.)
     */
    private static String getDirection(int dx, int dz) {
        // Calculate angle in degrees (0 = east, 90 = south, 180 = west, 270 = north)
        double angle = Math.toDegrees(Math.atan2(dz, dx));

        // Normalize to 0-360
        if (angle < 0) {
            angle += 360;
        }

        // Convert to Minecraft coordinates (0 = south, 90 = west, 180 = north, 270 = east)
        // In Minecraft: -Z is north, +Z is south, -X is west, +X is east
        // atan2(dz, dx) gives: dx > 0, dz = 0 → 0° (east)
        // We need to rotate to match Minecraft's coordinate system

        // Adjust angle: rotate by 90 degrees clockwise so that -Z (north) is 0°
        angle = (90 - angle + 360) % 360;

        // Map to 8 directions
        if (angle >= 337.5 || angle < 22.5) {
            return "north";
        } else if (angle >= 22.5 && angle < 67.5) {
            return "northeast";
        } else if (angle >= 67.5 && angle < 112.5) {
            return "east";
        } else if (angle >= 112.5 && angle < 157.5) {
            return "southeast";
        } else if (angle >= 157.5 && angle < 202.5) {
            return "south";
        } else if (angle >= 202.5 && angle < 247.5) {
            return "southwest";
        } else if (angle >= 247.5 && angle < 292.5) {
            return "west";
        } else {
            return "northwest";
        }
    }
}
