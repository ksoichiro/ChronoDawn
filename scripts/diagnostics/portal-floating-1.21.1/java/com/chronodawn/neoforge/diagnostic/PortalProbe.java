package com.chronodawn.neoforge.diagnostic;

import com.chronodawn.ChronoDawn;
import com.chronodawn.core.portal.PortalPlacementHelper;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.core.portal.PortalTeleportHandler;
import com.chronodawn.data.PortalRegistryData;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/** Opt-in regression probe for server lifecycle isolation and actual disk persistence. */
@EventBusSubscriber(modid = "chronodawn")
public final class PortalProbe {
    private static final BlockPos STALE_POS = new BlockPos(0, 220, 32);
    private static final Path EXPECTED_FILE = Path.of("portal-probe-expected.txt");
    private static PortalRegistryData previousData;
    private static BlockPos savedFrame;
    private static boolean completed;
    private static boolean failed;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void contaminateBeforeStart(ServerAboutToStartEvent event) {
        PortalRegistry registry = PortalRegistry.getInstance();
        previousData = new PortalRegistryData();
        registry.setSavedData(previousData);
        registry.registerPortal(new PortalStateMachine(UUID.randomUUID(),
            ModDimensions.CHRONO_DAWN_DIMENSION, STALE_POS));
        previousData.setDirty(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void verifyBeforeStart(ServerAboutToStartEvent event) {
        PortalRegistry registry = PortalRegistry.getInstance();
        check(registry.getAllPortals().isEmpty(), "previous server entries cleared before level load");
        registry.registerPortal(new PortalStateMachine(UUID.randomUUID(),
            ModDimensions.CHRONO_DAWN_DIMENSION, STALE_POS));
        check(!previousData.isDirty(), "previous SavedData detached before level load");
        registry.clear();
    }

    @SubscribeEvent
    public static void run(ServerStartedEvent event) {
        var server = event.getServer();
        try {
            ServerLevel level = server.getLevel(ModDimensions.CHRONO_DAWN_DIMENSION);
            if (level == null) throw new AssertionError("Chrono Dawn dimension missing");
            PortalRegistry registry = PortalRegistry.getInstance();
            check(registry.getPortalAt(level.dimension(), STALE_POS) == null, "stale midair entry absent");
            if (Files.exists(EXPECTED_FILE)) {
                String[] expected = Files.readString(EXPECTED_FILE).strip().split("\\s+");
                savedFrame = new BlockPos(Integer.parseInt(expected[1]), Integer.parseInt(expected[2]),
                    Integer.parseInt(expected[3]));
                PortalStateMachine loaded = registry.getPortalAt(level.dimension(), savedFrame);
                check(loaded != null && loaded.getPortalId().equals(UUID.fromString(expected[0])),
                    "saved portal UUID and position loaded from disk");
                check(registry.getAllPortals().size() == 1, "saved registry has no extra stale entries");
            } else {
                check(registry.getAllPortals().isEmpty(), "fresh world starts with empty registry");
                var lookup = PortalTeleportHandler.class.getDeclaredMethod("findNearbyPortal",
                    ServerLevel.class, BlockPos.class);
                lookup.setAccessible(true);
                lookup.invoke(null, level, new BlockPos(0, 150, 32));
                var reuse = PortalTeleportHandler.class.getDeclaredMethod("findReusablePortalFrame",
                    ServerLevel.class, BlockPos.class, Direction.Axis.class, ServerPlayer.class);
                reuse.setAccessible(true);
                Optional<?> reused = (Optional<?>) reuse.invoke(null, level, new BlockPos(0, 150, 32),
                    Direction.Axis.X, null);
                check(reused.isEmpty(), "stale midair portal cannot be restored in new world");
                BlockPos selected = PortalPlacementHelper.findSafeFramePosition(level,
                    new BlockPos(0, 79, 32), Direction.Axis.X, 8);
                if (selected == null) throw new AssertionError("No natural placement found");
                var generate = PortalTeleportHandler.class.getDeclaredMethod("generatePortal",
                    ServerLevel.class, BlockPos.class, Direction.Axis.class, ServerPlayer.class);
                generate.setAccessible(true);
                savedFrame = (BlockPos) generate.invoke(null, level, new BlockPos(0, 79, 32),
                    Direction.Axis.X, null);
                check(selected.equals(savedFrame), "actual portal generated at natural placement " + savedFrame);
                check(!level.getBlockState(savedFrame.offset(1, -1, 0)).isAir(), "generated portal has support");
                PortalStateMachine saved = registry.getPortalAt(level.dimension(), savedFrame);
                if (saved == null) throw new AssertionError("Generated frame not registered");
                Files.writeString(EXPECTED_FILE, saved.getPortalId() + " " + savedFrame.getX() + " "
                    + savedFrame.getY() + " " + savedFrame.getZ() + "\n");
            }
            completed = true;
        } catch (Throwable failure) {
            failed = true;
            ChronoDawn.LOGGER.error("PORTAL_PROBE FAILED", failure);
        } finally {
            server.halt(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void stopping(ServerStoppingEvent event) {
        check(savedFrame != null && PortalRegistry.getInstance()
            .getPortalAt(ModDimensions.CHRONO_DAWN_DIMENSION, savedFrame) != null,
            "portal retained until final save");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void stopped(ServerStoppedEvent event) {
        PortalRegistry registry = PortalRegistry.getInstance();
        check(registry.getAllPortals().isEmpty(), "registry empty after final save");
        PortalRegistryData savedData = PortalRegistryData.get(event.getServer().overworld());
        savedData.setDirty(false);
        registry.registerPortal(new PortalStateMachine(UUID.randomUUID(),
            ModDimensions.CHRONO_DAWN_DIMENSION, STALE_POS));
        check(!savedData.isDirty(), "SavedData detached after final save");
        registry.clear();
        Thread serverThread = Thread.currentThread();
        // Shutdown hooks join the server thread, so exit only after that thread finishes.
        Thread exitThread = new Thread(() -> {
            try {
                serverThread.join();
                System.exit(completed && !failed ? 0 : 1);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }, "portal-probe-exit");
        exitThread.setDaemon(true);
        exitThread.start();
    }

    private static void check(boolean condition, String description) {
        if (!condition) failed = true;
        ChronoDawn.LOGGER.info("PORTAL_PROBE {} {}", condition ? "PASS" : "FAIL", description);
    }
}
