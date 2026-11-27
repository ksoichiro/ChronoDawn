import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sampling profiler for the Minecraft server thread, loaded with -javaagent.
 *
 * It needs no change to the mod: every few milliseconds it takes the stack of
 * the thread named "Server thread" and counts how many samples have a
 * com.chronodawn frame. Wall time is 50 ms per tick on a server that keeps
 * 20 TPS, so a share of samples is a share of the tick budget.
 *
 * Usage: {@code -javaagent:tick-profile-agent.jar=<output dir>[,interval=<ms>]}.
 * The report is rewritten every 30 s and at JVM exit, as
 * {@code <output dir>/tick-profile-<pid>.txt}. A JVM without a "Server thread"
 * (Gradle, the client) writes nothing.
 */
public final class TickProfileAgent {
    private static final String MOD_PREFIX = "com.chronodawn.";
    private static final String SERVER_THREAD = "Server thread";
    private static final int TOP = 25;

    private static final Object LOCK = new Object();
    private static long total;
    private static long idle;
    private static long modSamples;
    private static final Map<String, Long> ENTRY = new HashMap<>();
    private static final Map<String, Long> INCLUSIVE = new HashMap<>();
    private static final Map<String, Long> LEAF = new HashMap<>();

    private TickProfileAgent() {
    }

    public static void premain(String args, Instrumentation instrumentation) {
        String dir = ".";
        long intervalMs = 2;
        if (args != null && !args.isEmpty()) {
            for (String part : args.split(",")) {
                if (part.startsWith("interval=")) {
                    intervalMs = Long.parseLong(part.substring("interval=".length()));
                } else {
                    dir = part;
                }
            }
        }
        Path out = Path.of(dir, "tick-profile-" + ProcessHandle.current().pid() + ".txt");
        long interval = intervalMs;
        Thread sampler = new Thread(() -> sample(out, interval), "tick-profile-sampler");
        sampler.setDaemon(true);
        sampler.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> write(out, interval)));
    }

    private static Thread findServerThread() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (SERVER_THREAD.equals(thread.getName())) {
                return thread;
            }
        }
        return null;
    }

    private static void sample(Path out, long interval) {
        Thread server = null;
        long lastWrite = System.nanoTime();
        while (true) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                return;
            }
            if (server == null || !server.isAlive()) {
                server = findServerThread();
                if (server == null) {
                    continue;
                }
            }
            StackTraceElement[] stack = server.getStackTrace();
            if (stack.length > 0) {
                record(stack);
            }
            if (System.nanoTime() - lastWrite > 30_000_000_000L) {
                write(out, interval);
                lastWrite = System.nanoTime();
            }
        }
    }

    private static void record(StackTraceElement[] stack) {
        boolean isIdle = false;
        String entry = null;
        String leaf = null;
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < stack.length; i++) {
            StackTraceElement frame = stack[i];
            String cls = frame.getClassName();
            // Mojang names only: on Fabric 1.21.1 the method is obfuscated, so idle stays 0 there and
            // only the mod share of wall time is meaningful.
            if (cls.endsWith("MinecraftServer") && frame.getMethodName().equals("waitUntilNextTick")) {
                isIdle = true;
            }
            if (cls.startsWith(MOD_PREFIX)) {
                String name = cls.substring(MOD_PREFIX.length()) + "." + frame.getMethodName();
                if (leaf == null) {
                    leaf = name + ":" + frame.getLineNumber();
                }
                entry = name;
                seen.add(name);
            }
        }
        synchronized (LOCK) {
            total++;
            if (isIdle) {
                idle++;
            }
            if (entry != null) {
                modSamples++;
                ENTRY.merge(entry, 1L, Long::sum);
                LEAF.merge(leaf, 1L, Long::sum);
                for (String name : seen) {
                    INCLUSIVE.merge(name, 1L, Long::sum);
                }
            }
        }
    }

    private static void write(Path out, long interval) {
        List<String> lines = new ArrayList<>();
        synchronized (LOCK) {
            if (total == 0) {
                return;
            }
            long busy = total - idle;
            lines.add(String.format("samples=%d interval_ms=%d idle=%d busy=%d", total, interval, idle, busy));
            lines.add(String.format("mod_samples=%d mod_share_of_wall=%.3f%% mod_ms_per_tick=%.4f"
                + " busy_ms_per_tick=%.3f", modSamples, 100.0 * modSamples / total,
                50.0 * modSamples / total, 50.0 * busy / total));
            section(lines, "ENTRY (outermost com.chronodawn frame, one per sample)", ENTRY);
            section(lines, "INCLUSIVE (every com.chronodawn method on the stack)", INCLUSIVE);
            section(lines, "LEAF (innermost com.chronodawn frame, with line)", LEAF);
        }
        try {
            Files.write(out, lines);
        } catch (Exception e) {
            // Reporting must never disturb the server.
        }
    }

    private static void section(List<String> lines, String title, Map<String, Long> counts) {
        lines.add("");
        lines.add("## " + title);
        counts.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(TOP)
            .forEach(e -> lines.add(String.format("%7d  %6.3f ms/tick  %s", e.getValue(),
                50.0 * e.getValue() / Math.max(1, total), e.getKey())));
    }
}
