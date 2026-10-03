package com.chronodawn.neoforge.client;

/**
 * Development-only workaround that lets the NeoForge dev client exit promptly.
 *
 * Minecraft 26.x starts a post-main shutdown watchdog before returning from its
 * main method. The NeoForge development launcher closes the Minecraft module but
 * does not terminate the JVM while launcher threads are still alive. The watchdog
 * then fails to load {@code ServerWatchdog} from the closed module and cannot
 * finish terminating the process.
 *
 * Remove this once the development launcher terminates normally after Minecraft
 * returns.
 */
public final class DevShutdownWorkaround {
    private static boolean initialized;

    private DevShutdownWorkaround() {
    }

    /**
     * Must first be called from a client tick so it captures Minecraft's main
     * thread rather than a parallel mod-loading worker.
     */
    public static void register() {
        if (initialized) {
            return;
        }
        initialized = true;

        if (!Boolean.getBoolean("chronodawn.dev.shutdownWorkaround")) {
            return;
        }

        Thread mainThread = Thread.currentThread();
        Thread waiter = new Thread(() -> {
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                return;
            }
            // The launcher has completed Minecraft and NeoForge shutdown by now.
            // Crash paths call System.exit themselves, so this handles normal exit only.
            System.exit(0);
        }, "ChronoDawn dev shutdown");
        waiter.setDaemon(true);
        waiter.start();
    }
}
