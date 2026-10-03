package com.chronodawn.fabric.client;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Development-only workaround that lets the Fabric dev client exit promptly.
 *
 * Architectury Loom launches the dev client through architectury-transformer's
 * TransformerRuntime, which leaves two non-daemon thread pools running
 * (https://github.com/architectury/architectury-transformer/issues/8). They keep
 * the JVM alive after Minecraft's main method returns. Minecraft 26.x starts a
 * "post-main" ClientShutdownWatchdog right before returning, so every normal quit
 * ends in a "Watchdog (Client shutdown from post-main)" crash report 15 seconds later.
 *
 * Remove this once the upstream issue is fixed.
 */
public final class DevShutdownWorkaround {
    private DevShutdownWorkaround() {
    }

    /**
     * Must be called from the client initializer, which runs on the JVM main thread
     * (renamed to "Render thread" by Minecraft).
     */
    public static void register() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        Thread mainThread = Thread.currentThread();
        Thread waiter = new Thread(() -> {
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                return;
            }
            // Minecraft has finished its own shutdown by the time main returns.
            // Crash paths call System.exit themselves, so this only handles normal exit.
            System.exit(0);
        }, "ChronoDawn dev shutdown");
        waiter.setDaemon(true);
        waiter.start();
    }
}
