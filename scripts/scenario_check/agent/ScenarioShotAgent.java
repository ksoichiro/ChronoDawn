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
 *   <li>{@code click <dx> <dy> [button]}: click inside the open screen at
 *       GUI-scaled coordinates relative to the screen center (button 0 is
 *       left, the default), for buttons and lists.</li>
 *   <li>{@code slot <index> [button] [pickup|quick_move]}: click a slot of the
 *       open container (a shift-click with quick_move), the way the game sends it.</li>
 *   <li>{@code hover <dx> <dy>}, {@code hover_slot <index>}: park the mouse
 *       there so the next frame draws the tooltip.</li>
 *   <li>{@code scroll <dx> <dy> <amount>}: scroll the wheel there, positive
 *       amounts scroll up.</li>
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

    /** Mojang names that only Minecraft 26.x uses. */
    private static final String GAME_RENDERER = "gameRenderer";
    private static final String MAIN_RENDER_TARGET = "mainRenderTarget";
    private static final String GUI = "gui";
    private static final String GUI_SCREEN = "screen";

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

    private record Target(Object minecraft, Getter renderTarget, Method grab, boolean grabTakesDownscale,
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
                    return new Target(minecraft, renderTarget, grab, grabTakesDownscale,
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
                    String[] args = operation.split("\\s+");
                    switch (args[0]) {
                        case "", "shot" -> shoot(dir, name);
                        case "use" -> writeDone(dir, name, click(keyUse));
                        case "attack" -> writeDone(dir, name, click(keyAttack));
                        case "close" -> writeDone(dir, name, close());
                        case "screen" -> writeDone(dir, name, title());
                        case "click", "slot", "hover", "hover_slot", "scroll" ->
                                writeDone(dir, name, screenInput(args));
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

        /**
         * Input inside an open screen. These names are looked up when first
         * used, because Minecraft 26.x changed the click methods and only the
         * 1.21.1 shape is supported.
         */
        private String screenInput(String[] args) throws ReflectiveOperationException {
            Object localPlayer = player.get(minecraft);
            Object current = screen.get(minecraft);
            if (localPlayer == null || current == null) {
                return "error: no screen is open";
            }
            Class<?> screenClass = current.getClass();
            Class<?> minecraftClass = minecraft.getClass();
            Object window = lookup(minecraftClass, "getWindow", "method_22683").invoke(minecraft);
            Class<?> windowClass = window.getClass();
            int guiWidth = (int) lookup(windowClass, "getGuiScaledWidth", "method_4486").invoke(window);
            int guiHeight = (int) lookup(windowClass, "getGuiScaledHeight", "method_4502").invoke(window);
            String op = args[0];
            double x;
            double y;
            if (op.equals("slot")) {
                return slotClick(localPlayer, args);
            }
            if (op.equals("hover_slot")) {
                Object slot = slotAt(localPlayer, Integer.parseInt(args[1]));
                Class<?> container = containerScreen(current);
                if (container == null) {
                    return "error: the open screen is not a container screen";
                }
                x = intField(container, current, "leftPos", "field_2776")
                        + (int) slotField(slot, "x", "field_7873") + 8;
                y = intField(container, current, "topPos", "field_2800")
                        + (int) slotField(slot, "y", "field_7872") + 8;
            } else {
                x = guiWidth / 2.0 + Double.parseDouble(args[1]);
                y = guiHeight / 2.0 + Double.parseDouble(args[2]);
            }
            switch (op) {
                case "click" -> {
                    int button = args.length > 3 ? Integer.parseInt(args[3]) : 0;
                    Method clicked = lookup(screenClass, "mouseClicked", "method_25402", double.class, double.class, int.class);
                    Method released = lookup(screenClass, "mouseReleased", "method_25406", double.class, double.class, int.class);
                    boolean handled = (boolean) clicked.invoke(current, x, y, button);
                    released.invoke(current, x, y, button);
                    return handled ? "ok" : "error: nothing at " + args[1] + " " + args[2] + " took the click";
                }
                case "scroll" -> {
                    Method scrolled = lookup(screenClass, "mouseScrolled", "method_25401",
                            double.class, double.class, double.class, double.class);
                    boolean handled = (boolean) scrolled.invoke(current, x, y, 0.0, Double.parseDouble(args[3]));
                    return handled ? "ok" : "error: nothing at " + args[1] + " " + args[2] + " scrolled";
                }
                default -> {
                    // The game draws the tooltip for the position its mouse handler holds.
                    int screenWidth = (int) lookup(windowClass, "getScreenWidth", "method_4480").invoke(window);
                    double scale = (double) screenWidth / guiWidth;
                    Object mouse = fieldOf(minecraftClass, "mouseHandler", "field_1729").get(minecraft);
                    setDouble(mouse, "xpos", "field_1795", x * scale);
                    setDouble(mouse, "ypos", "field_1794", y * scale);
                    lookup(screenClass, "mouseMoved", "method_16014", double.class, double.class)
                            .invoke(current, x, y);
                    return "ok";
                }
            }
        }

        private String slotClick(Object localPlayer, String[] args) throws ReflectiveOperationException {
            int index = Integer.parseInt(args[1]);
            int button = args.length > 2 ? Integer.parseInt(args[2]) : 0;
            String action = args.length > 3 ? args[3] : "pickup";
            Object menu = fieldOf(localPlayer.getClass(), "containerMenu", "field_7512").get(localPlayer);
            slotAt(localPlayer, index);
            int containerId = (int) fieldOf(menu.getClass(), "containerId", "field_7763").get(menu);
            Object gameMode = fieldOf(minecraft.getClass(), "gameMode", "field_1761").get(minecraft);
            Method handle = null;
            for (Method candidate : gameMode.getClass().getMethods()) {
                Class<?>[] types = candidate.getParameterTypes();
                if ((candidate.getName().equals("handleInventoryMouseClick") || candidate.getName().equals("method_2906"))
                        && types.length == 5 && types[3].isEnum()) {
                    handle = candidate;
                }
            }
            if (handle == null) {
                return "error: no handleInventoryMouseClick on this version";
            }
            // PICKUP and QUICK_MOVE are the first two constants of ClickType.
            int ordinal = switch (action) {
                case "pickup" -> 0;
                case "quick_move" -> 1;
                default -> -1;
            };
            if (ordinal < 0) {
                return "error: unknown slot action " + action;
            }
            handle.invoke(gameMode, containerId, index, button,
                    handle.getParameterTypes()[3].getEnumConstants()[ordinal], localPlayer);
            return "ok";
        }

        private Object slotAt(Object localPlayer, int index) throws ReflectiveOperationException {
            Object menu = fieldOf(localPlayer.getClass(), "containerMenu", "field_7512").get(localPlayer);
            return lookup(menu.getClass(), "getSlot", "method_7611", int.class).invoke(menu, index);
        }

        private String title() throws ReflectiveOperationException {
            Object current = screen.get(minecraft);
            return current == null ? "" : text(getTitle.invoke(current));
        }
    }

    /** The AbstractContainerScreen class in the chain, or null for other screens. */
    private static Class<?> containerScreen(Object screen) {
        for (Class<?> c = screen.getClass(); c != null; c = c.getSuperclass()) {
            for (String name : new String[] {"leftPos", "field_2776"}) {
                try {
                    c.getDeclaredField(name);
                    return c;
                } catch (NoSuchFieldException ignored) {
                    // try the next name
                }
            }
        }
        return null;
    }

    private static Method lookup(Class<?> owner, String mojang, String intermediary, Class<?>... parameters)
            throws NoSuchMethodException {
        for (Class<?> c = owner; c != null; c = c.getSuperclass()) {
            for (String name : new String[] {mojang, intermediary}) {
                try {
                    Method method = c.getDeclaredMethod(name, parameters);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                    // try the next name or class
                }
            }
        }
        for (Class<?> c = owner; c != null; c = c.getSuperclass()) {
            for (Class<?> type : c.getInterfaces()) {
                try {
                    return lookup(type, mojang, intermediary, parameters);
                } catch (NoSuchMethodException ignored) {
                    // try the next interface
                }
            }
        }
        throw new NoSuchMethodException(mojang + "/" + intermediary + " on " + owner.getName());
    }

    private static Field fieldOf(Class<?> owner, String mojang, String intermediary) throws NoSuchFieldException {
        for (Class<?> c = owner; c != null; c = c.getSuperclass()) {
            for (String name : new String[] {mojang, intermediary}) {
                try {
                    Field field = c.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                    // try the next name or class
                }
            }
        }
        throw new NoSuchFieldException(mojang + "/" + intermediary + " on " + owner.getName());
    }

    private static int intField(Class<?> owner, Object target, String mojang, String intermediary)
            throws ReflectiveOperationException {
        return (int) fieldOf(owner, mojang, intermediary).get(target);
    }

    private static int slotField(Object slot, String mojang, String intermediary) throws ReflectiveOperationException {
        return intField(slot.getClass(), slot, mojang, intermediary);
    }

    private static void setDouble(Object target, String mojang, String intermediary, double value)
            throws ReflectiveOperationException {
        fieldOf(target.getClass(), mojang, intermediary).setDouble(target, value);
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
