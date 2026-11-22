import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Java agent that lets the scenario check driver take screenshots and send
 * player input inside the Minecraft 1.21.1 client.
 *
 * Minecraft builds the image from its own framebuffer, the way F2 does, so the
 * capture does not go through macOS screen recording. That keeps working while
 * the screen is locked or the window is covered, and needs no Screen Recording
 * permission. Input goes through the game's key mappings rather than the OS,
 * for the same reason.
 *
 * The driver writes {@code <dir>/<name>.request} whose content is one
 * operation:
 * <ul>
 *   <li>{@code shot} (or empty): save {@code <dir>/screenshots/<name>.png}.
 *       The done marker holds the chat message Minecraft produced.</li>
 *   <li>{@code use}, {@code attack}: click the use (right) or attack (left)
 *       mouse button once. The game acts on it in its next tick, on whatever
 *       the crosshair points at.</li>
 *   <li>{@code close}: close the open screen, telling the server when it is a
 *       container screen.</li>
 *   <li>{@code screen}: report the title of the open screen, empty when none.</li>
 * </ul>
 * The agent deletes the request, runs it on the render thread, and writes
 * {@code <dir>/<name>.done} with the result. A result starting with
 * {@code error:} means the operation did not run.
 *
 * It is an agent rather than a mod so one class works on every loader without
 * a build: only the names differ, Mojang names on NeoForge and intermediary
 * names on Fabric, and both sets are listed below.
 */
public final class ScenarioShotAgent {
    private record Names(String minecraft, String getInstance, String getMainRenderTarget,
                         String screenshot, String grab,
                         String options, String player, String screen,
                         String keyUse, String keyAttack, String keyMapping, String click, String getDefaultKey,
                         String screenClass, String getTitle, String closeContainer) {
    }

    private static final Names[] NAMES = {
            new Names("net.minecraft.client.Minecraft", "getInstance", "getMainRenderTarget",
                    "net.minecraft.client.Screenshot", "grab",
                    "options", "player", "screen",
                    "keyUse", "keyAttack", "net.minecraft.client.KeyMapping", "click", "getDefaultKey",
                    "net.minecraft.client.gui.screens.Screen", "getTitle", "closeContainer"),
            new Names("net.minecraft.class_310", "method_1551", "method_1522",
                    "net.minecraft.class_318", "method_22690",
                    "field_1690", "field_1724", "field_1755",
                    "field_1904", "field_1886", "net.minecraft.class_304", "method_1420", "method_1429",
                    "net.minecraft.class_437", "method_25440", "method_7346"),
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
                        String operation = Files.readString(request, StandardCharsets.UTF_8).strip();
                        Files.delete(request);
                        target.run(dir, name, operation);
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

    private record Target(Object minecraft, Method getMainRenderTarget, Method grab,
                          Field options, Field player, Field screen,
                          Field keyUse, Field keyAttack, Method click, Method getDefaultKey,
                          Method getTitle, Method closeContainer) {
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
                    ClassLoader loader = loaded.getClassLoader();
                    Method getMainRenderTarget = loaded.getMethod(names.getMainRenderTarget());
                    Class<?> screenshot = Class.forName(names.screenshot(), false, loader);
                    Method grab = screenshot.getMethod(names.grab(), File.class, String.class,
                            getMainRenderTarget.getReturnType(), Consumer.class);
                    Field options = loaded.getField(names.options());
                    Field player = loaded.getField(names.player());
                    Field screen = loaded.getField(names.screen());
                    Class<?> optionsClass = options.getType();
                    Class<?> keyMapping = Class.forName(names.keyMapping(), false, loader);
                    Method getDefaultKey = keyMapping.getMethod(names.getDefaultKey());
                    Method click = keyMapping.getMethod(names.click(), getDefaultKey.getReturnType());
                    Class<?> screenClass = Class.forName(names.screenClass(), false, loader);
                    return new Target(minecraft, getMainRenderTarget, grab,
                            options, player, screen,
                            optionsClass.getField(names.keyUse()), optionsClass.getField(names.keyAttack()),
                            click, getDefaultKey,
                            screenClass.getMethod(names.getTitle()),
                            player.getType().getMethod(names.closeContainer()));
                }
            }
            return null;
        }

        void run(Path dir, String name, String operation) {
            // Minecraft is an Executor whose tasks run on the render thread,
            // where the framebuffer can be read and input is handled.
            ((Executor) minecraft).execute(() -> {
                try {
                    switch (operation) {
                        case "", "shot" -> shoot(dir, name);
                        case "use" -> writeDone(dir, name, click(keyUse));
                        case "attack" -> writeDone(dir, name, click(keyAttack));
                        case "close" -> writeDone(dir, name, close());
                        case "screen" -> writeDone(dir, name, title());
                        default -> writeDone(dir, name, "error: unknown operation " + operation);
                    }
                } catch (Throwable t) {
                    writeDone(dir, name, "error: " + t);
                }
            });
        }

        private void shoot(Path dir, String name) throws ReflectiveOperationException {
            Object renderTarget = getMainRenderTarget.invoke(minecraft);
            Consumer<Object> done = message -> writeDone(dir, name, message);
            grab.invoke(null, dir.toFile(), name + ".png", renderTarget, done);
        }

        private String click(Field key) throws ReflectiveOperationException {
            if (player.get(minecraft) == null) {
                return "error: no player in a world";
            }
            if (screen.get(minecraft) != null) {
                // The game only handles clicks with no screen open. A queued
                // click would fire once the screen closes, at an unknown time.
                return "error: a screen is open: " + title();
            }
            // The driver writes a fresh options.txt, so the default binding is
            // the one in effect.
            Object mapping = key.get(options.get(minecraft));
            click.invoke(null, getDefaultKey.invoke(mapping));
            return "ok";
        }

        private String close() throws ReflectiveOperationException {
            Object localPlayer = player.get(minecraft);
            if (screen.get(minecraft) == null || localPlayer == null) {
                return "ok: no screen";
            }
            // LocalPlayer.closeContainer tells the server and clears the
            // screen, so a trader or chest is released as well.
            closeContainer.invoke(localPlayer);
            return "ok";
        }

        private String title() throws ReflectiveOperationException {
            Object current = screen.get(minecraft);
            return current == null ? "" : text(getTitle.invoke(current));
        }
    }

    private static String text(Object message) {
        try {
            // Component extends Brigadier's Message, whose getString is not remapped.
            return message instanceof String s ? s : (String) message.getClass().getMethod("getString").invoke(message);
        } catch (ReflectiveOperationException e) {
            return String.valueOf(message);
        }
    }

    private static void writeDone(Path dir, String name, Object message) {
        String text = text(message);
        try {
            Path tmp = dir.resolve(name + ".done.tmp");
            Files.writeString(tmp, text, StandardCharsets.UTF_8);
            Files.move(tmp, dir.resolve(name + ".done"));
        } catch (Exception e) {
            System.err.println("[scenario-shot-agent] cannot write done marker for " + name + ": " + e);
        }
    }
}
