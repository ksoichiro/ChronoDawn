import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Java agent that lets the scenario check driver take screenshots inside the
 * Minecraft 1.21.1 client, the way F2 does.
 *
 * Minecraft builds the image from its own framebuffer, so the capture does not
 * go through macOS screen recording. That keeps working while the screen is
 * locked or the window is covered, and needs no Screen Recording permission.
 *
 * The driver writes {@code <dir>/<name>.request}. The agent deletes it, asks
 * the render thread to save {@code <dir>/screenshots/<name>.png}, and writes
 * {@code <dir>/<name>.done} with the chat message Minecraft produced once the
 * file is saved (or failed to save).
 *
 * It is an agent rather than a mod so one class works on every loader without
 * a build: only the names differ, Mojang names on NeoForge and intermediary
 * names on Fabric, and both sets are listed below.
 */
public final class ScenarioShotAgent {
    private record Names(String minecraft, String getInstance, String getMainRenderTarget,
                         String screenshot, String grab) {
    }

    private static final Names[] NAMES = {
            new Names("net.minecraft.client.Minecraft", "getInstance", "getMainRenderTarget",
                    "net.minecraft.client.Screenshot", "grab"),
            new Names("net.minecraft.class_310", "method_1551", "method_1522",
                    "net.minecraft.class_318", "method_22690"),
    };

    private static final long POLL_MILLIS = 200;

    private ScenarioShotAgent() {
    }

    public static void premain(String args, Instrumentation instrumentation) {
        Path dir = Path.of(args);
        Thread thread = new Thread(() -> loop(dir, instrumentation), "scenario-shot-agent");
        thread.setDaemon(true);
        thread.start();
    }

    private static void loop(Path dir, Instrumentation instrumentation) {
        Target target = null;
        while (true) {
            try {
                Thread.sleep(POLL_MILLIS);
                Files.createDirectories(dir);
                try (var requests = Files.newDirectoryStream(dir, "*.request")) {
                    for (Path request : requests) {
                        String file = request.getFileName().toString();
                        String name = file.substring(0, file.length() - ".request".length());
                        if (target == null) {
                            target = Target.find(instrumentation);
                        }
                        if (target == null) {
                            // The game has not loaded its classes yet. Keep the request.
                            break;
                        }
                        Files.delete(request);
                        target.shoot(dir, name);
                    }
                }
            } catch (InterruptedException e) {
                return;
            } catch (Throwable t) {
                System.err.println("[scenario-shot-agent] " + t);
                t.printStackTrace();
            }
        }
    }

    private record Target(Object minecraft, Method getMainRenderTarget, Method grab) {
        static Target find(Instrumentation instrumentation) throws ReflectiveOperationException {
            for (Class<?> loaded : instrumentation.getAllLoadedClasses()) {
                for (Names names : NAMES) {
                    if (!loaded.getName().equals(names.minecraft())) {
                        continue;
                    }
                    Object minecraft = loaded.getMethod(names.getInstance()).invoke(null);
                    if (minecraft == null) {
                        return null;
                    }
                    Method getMainRenderTarget = loaded.getMethod(names.getMainRenderTarget());
                    Class<?> screenshot = Class.forName(names.screenshot(), false, loaded.getClassLoader());
                    Method grab = screenshot.getMethod(names.grab(), File.class, String.class,
                            getMainRenderTarget.getReturnType(), Consumer.class);
                    return new Target(minecraft, getMainRenderTarget, grab);
                }
            }
            return null;
        }

        void shoot(Path dir, String name) {
            // Minecraft is an Executor whose tasks run on the render thread,
            // where the framebuffer can be read.
            ((java.util.concurrent.Executor) minecraft).execute(() -> {
                try {
                    Object renderTarget = getMainRenderTarget.invoke(minecraft);
                    Consumer<Object> done = message -> writeDone(dir, name, message);
                    grab.invoke(null, dir.toFile(), name + ".png", renderTarget, done);
                } catch (Throwable t) {
                    writeDone(dir, name, "agent error: " + t);
                }
            });
        }
    }

    private static void writeDone(Path dir, String name, Object message) {
        String text;
        try {
            // Component extends Brigadier's Message, whose getString is not remapped.
            text = message instanceof String s ? s : (String) message.getClass().getMethod("getString").invoke(message);
        } catch (ReflectiveOperationException e) {
            text = String.valueOf(message);
        }
        try {
            Path tmp = dir.resolve(name + ".done.tmp");
            Files.writeString(tmp, text, StandardCharsets.UTF_8);
            Files.move(tmp, dir.resolve(name + ".done"));
        } catch (Exception e) {
            System.err.println("[scenario-shot-agent] cannot write done marker for " + name + ": " + e);
        }
    }
}
