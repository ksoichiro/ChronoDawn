import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Java agent that lets the scenario check driver take screenshots and send
 * player input inside the Minecraft 1.21.1 and 26.x clients.
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
 *   <li>{@code render}: report how far the world around the player has
 *       loaded and rendered, as {@code chunks=<n> missing=<n> sections=<n>
 *       queue=<empty|busy>}. {@code chunks} is the number of chunks the
 *       client holds, {@code missing} the number it still lacks within
 *       {@value #RENDER_RADIUS} chunks of the player, {@code sections} the
 *       number of visible sections, and {@code queue} whether section
 *       builds are still queued. The driver waits until these stop
 *       changing instead of sleeping a fixed time.</li>
 * </ul>
 * The agent deletes the request, runs it on the render thread, and writes
 * {@code <dir>/<name>.done} with the result. A result starting with
 * {@code error:} means the operation did not run.
 *
 * It is an agent rather than a mod so one class works on every loader without
 * a build: only the names differ, Mojang names on NeoForge and intermediary
 * names on Fabric 1.21.1, and both sets are listed below. Minecraft 26.x is
 * unobfuscated, so both loaders use Mojang names there. It moved the main
 * render target to {@code GameRenderer.mainRenderTarget()} and the open
 * screen to {@code Gui.screen()}, and {@code Screenshot.grab} takes a
 * downscale factor. {@link Target#find} tries the 1.21.1 shape first and falls
 * back to the 26.x one.
 */
public final class ScenarioShotAgent {
    private record Names(String minecraft, String getInstance, String getMainRenderTarget,
                         String screenshot, String grab,
                         String options, String player, String screen,
                         String keyUse, String keyAttack, String keyMapping, String click, String getDefaultKey,
                         String screenClass, String getTitle, String closeContainer,
                         String levelRenderer, String level, String getChunkSource, String hasChunk,
                         String getLoadedChunksCount, String getBlockX, String getBlockZ,
                         String hasRenderedAllSections, String countRenderedSections) {
    }

    private static final Names[] NAMES = {
            new Names("net.minecraft.client.Minecraft", "getInstance", "getMainRenderTarget",
                    "net.minecraft.client.Screenshot", "grab",
                    "options", "player", "screen",
                    "keyUse", "keyAttack", "net.minecraft.client.KeyMapping", "click", "getDefaultKey",
                    "net.minecraft.client.gui.screens.Screen", "getTitle", "closeContainer",
                    "levelRenderer", "level", "getChunkSource", "hasChunk",
                    "getLoadedChunksCount", "getBlockX", "getBlockZ",
                    "hasRenderedAllSections", "countRenderedSections"),
            new Names("net.minecraft.class_310", "method_1551", "method_1522",
                    "net.minecraft.class_318", "method_22690",
                    "field_1690", "field_1724", "field_1755",
                    "field_1904", "field_1886", "net.minecraft.class_304", "method_1420", "method_1429",
                    "net.minecraft.class_437", "method_25440", "method_7346",
                    "field_1769", "field_1687", "method_2935", "method_12123",
                    "method_14151", "method_31477", "method_31479",
                    "method_3281", "method_3246"),
    };

    /** Mojang names that only Minecraft 26.x uses. */
    private static final String GAME_RENDERER = "gameRenderer";
    private static final String MAIN_RENDER_TARGET = "mainRenderTarget";
    private static final String GUI = "gui";
    private static final String GUI_SCREEN = "screen";
    /** 26.x has no countRenderedSections, only the list of visible sections. */
    private static final String VISIBLE_SECTIONS = "visibleSections";

    /**
     * Chunks within this distance of the player must be loaded before a shot.
     * The test server's view-distance is 4, and the side shots look at
     * targets about 3 chunks away.
     */
    private static final int RENDER_RADIUS = 3;

    // Short, so a driver that polls render state waits little per answer.
    private static final long POLL_MILLIS = 50;

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

    /** A getter on the Minecraft instance that may go through one intermediate object. */
    private record Getter(Field via, Member member) {
        Object get(Object minecraft) throws ReflectiveOperationException {
            Object owner = via == null ? minecraft : via.get(minecraft);
            return member instanceof Method method ? method.invoke(owner) : ((Field) member).get(owner);
        }

        Class<?> type() {
            return member instanceof Method method ? method.getReturnType() : ((Field) member).getType();
        }

        /** {@code direct} on Minecraft (1.21.1), else {@code via.method()} (26.x). */
        static Getter find(Class<?> minecraft, Member direct, String via, String method)
                throws ReflectiveOperationException {
            if (direct != null) {
                return new Getter(null, direct);
            }
            Field field = minecraft.getField(via);
            return new Getter(field, field.getType().getMethod(method));
        }
    }

    private record Target(Object minecraft, Names names, Getter renderTarget, Method grab, boolean grabTakesDownscale,
                          Field options, Field player, Getter screen,
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
                    Getter renderTarget = Getter.find(loaded, method(loaded, names.getMainRenderTarget()),
                            GAME_RENDERER, MAIN_RENDER_TARGET);
                    Class<?> screenshot = Class.forName(names.screenshot(), false, loader);
                    Method grab = method(screenshot, names.grab(), File.class, String.class,
                            renderTarget.type(), Consumer.class);
                    boolean grabTakesDownscale = grab == null;
                    if (grabTakesDownscale) {
                        grab = screenshot.getMethod(names.grab(), File.class, String.class,
                                renderTarget.type(), int.class, Consumer.class);
                    }
                    Field options = loaded.getField(names.options());
                    Field player = loaded.getField(names.player());
                    Getter screen = Getter.find(loaded, field(loaded, names.screen()), GUI, GUI_SCREEN);
                    Class<?> optionsClass = options.getType();
                    Class<?> keyMapping = Class.forName(names.keyMapping(), false, loader);
                    Method getDefaultKey = keyMapping.getMethod(names.getDefaultKey());
                    Method click = keyMapping.getMethod(names.click(), getDefaultKey.getReturnType());
                    Class<?> screenClass = Class.forName(names.screenClass(), false, loader);
                    return new Target(minecraft, names, renderTarget, grab, grabTakesDownscale,
                            options, player, screen,
                            optionsClass.getField(names.keyUse()), optionsClass.getField(names.keyAttack()),
                            click, getDefaultKey,
                            screenClass.getMethod(names.getTitle()),
                            player.getType().getMethod(names.closeContainer()));
                }
            }
            return null;
        }

        private static Method method(Class<?> owner, String name, Class<?>... parameters) {
            try {
                return owner.getMethod(name, parameters);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private static Field field(Class<?> owner, String name) {
            try {
                return owner.getField(name);
            } catch (NoSuchFieldException e) {
                return null;
            }
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
                        case "render" -> writeDone(dir, name, render());
                        default -> writeDone(dir, name, "error: unknown operation " + operation);
                    }
                } catch (Throwable t) {
                    writeDone(dir, name, "error: " + t);
                }
            });
        }

        private void shoot(Path dir, String name) throws ReflectiveOperationException {
            Object target = renderTarget.get(minecraft);
            Consumer<Object> done = message -> writeDone(dir, name, message);
            if (grabTakesDownscale) {
                grab.invoke(null, dir.toFile(), name + ".png", target, 1, done);
            } else {
                grab.invoke(null, dir.toFile(), name + ".png", target, done);
            }
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

        private String render() throws ReflectiveOperationException {
            Class<?> minecraftClass = minecraft.getClass();
            Object level = field(minecraftClass, names.level()).get(minecraft);
            Object localPlayer = player.get(minecraft);
            if (level == null || localPlayer == null) {
                return "error: no player in a world";
            }
            Object chunkSource = level.getClass().getMethod(names.getChunkSource()).invoke(level);
            Method hasChunk = chunkSource.getClass().getMethod(names.hasChunk(), int.class, int.class);
            int chunks = (int) chunkSource.getClass().getMethod(names.getLoadedChunksCount()).invoke(chunkSource);
            int centerX = ((int) localPlayer.getClass().getMethod(names.getBlockX()).invoke(localPlayer)) >> 4;
            int centerZ = ((int) localPlayer.getClass().getMethod(names.getBlockZ()).invoke(localPlayer)) >> 4;
            int missing = 0;
            for (int dx = -RENDER_RADIUS; dx <= RENDER_RADIUS; dx++) {
                for (int dz = -RENDER_RADIUS; dz <= RENDER_RADIUS; dz++) {
                    if (!(boolean) hasChunk.invoke(chunkSource, centerX + dx, centerZ + dz)) {
                        missing++;
                    }
                }
            }
            Object renderer = field(minecraftClass, names.levelRenderer()).get(minecraft);
            Class<?> rendererClass = renderer.getClass();
            boolean queueEmpty = (boolean) rendererClass.getMethod(names.hasRenderedAllSections()).invoke(renderer);
            Method count = method(rendererClass, names.countRenderedSections());
            int sections = count != null
                    ? (int) count.invoke(renderer)
                    : ((java.util.List<?>) rendererClass.getMethod(VISIBLE_SECTIONS).invoke(renderer)).size();
            return "chunks=" + chunks + " missing=" + missing + " sections=" + sections
                    + " queue=" + (queueEmpty ? "empty" : "busy");
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
