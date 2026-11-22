package dev.ool.visuals;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** value == true means: this visual effect is REMOVED. */
public final class VisualsConfig {

    public static final class Option {
        public final String key;
        public final String label;
        public boolean value;

        Option(String key, String label, boolean def) {
            this.key = key;
            this.label = label;
            this.value = def;
        }
    }

    public static final Option SHAKE = new Option("noShake", "Тряска камеры", true);
    public static final Option TOTEM = new Option("noTotem", "Анимация тотема", true);
    public static final Option FOG = new Option("noFog", "Туман", true);
    public static final Option BLOCK_OVERLAY = new Option("noBlockOverlay", "Блоки на экране", true);
    public static final Option ARROWS = new Option("noArrows", "Стрелы в игроке", true);
    public static final Option BREAK_PARTICLES = new Option("noBreakParticles", "Частицы разрушения", true);
    public static final Option ALL_PARTICLES = new Option("noParticles", "Все частицы", false);

    /** Aspect ratio of the 3D picture (width / height). Window aspect is the normal look. */
    public static final float ASPECT_MIN = 0.80F;
    public static final float ASPECT_MAX = 2.50F;
    public static final float[] ASPECT_PRESETS = {1.00F, 1.25F, 1.33F, 1.60F, 2.33F};
    public static boolean aspectEnabled = false;
    public static float aspectRatio = 1.33F;

    /** show the distance of your last hit on a player */
    public static boolean hitDistance = true;

    /** Auto sprint: while you hold W you sprint without double-tapping (toggled by a key or in /visuals) */
    public static boolean autoSprint = false;

    /** Fast Block Place: hold the use button, blocks are placed every N ticks (vanilla = 4) */
    public static boolean fastPlace = false;
    public static int fastPlaceDelay = 2;   // 1..3

    public static final List<Option> OPTIONS = List.of(
            SHAKE, TOTEM, FOG, BLOCK_OVERLAY, ARROWS, BREAK_PARTICLES, ALL_PARTICLES);

    /** 1.0 = unchanged. >1 = narrower picture (objects look wider), <1 = wider picture. */
    public static float aspectScale() {
        if (!aspectEnabled) {
            return 1.0F;
        }
        try {
            Window w = Minecraft.getInstance().getWindow();
            if (w.getHeight() <= 0) {
                return 1.0F;
            }
            float windowAspect = (float) w.getWidth() / (float) w.getHeight();
            return windowAspect / aspectRatio;
        } catch (Throwable t) {
            return 1.0F;
        }
    }

    private static Matrix4f lastScaled = null;

    /**
     * Applies the Aspect X-scale to a projection matrix. Safe to call from several places:
     * a matrix that was already scaled (same object, or its X scale already matches) is returned as is,
     * so the picture can never be scaled twice.
     */
    public static Matrix4f applyAspect(Matrix4f m) {
        float s = aspectScale();
        if (Math.abs(s - 1.0F) < 0.0001F || m == null || m == lastScaled) {
            return m;
        }
        try {
            Window w = Minecraft.getInstance().getWindow();
            float windowAspect = (float) w.getWidth() / (float) w.getHeight();
            float unscaledX = m.m11() / windowAspect;      // m00 of a plain perspective matrix
            float tol = 0.003F * Math.abs(unscaledX) + 1.0E-5F;
            boolean looksUnscaled = Math.abs(m.m00() - unscaledX) <= tol;
            boolean looksScaled = Math.abs(m.m00() - unscaledX * s) <= tol;
            if (looksScaled && !looksUnscaled) {
                return m;
            }
        } catch (Throwable t) {
            return m;
        }
        Matrix4f copy = new Matrix4f(m);
        copy.m00(copy.m00() * s);
        lastScaled = copy;
        return copy;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("oolvisuals.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(f)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Option o : OPTIONS) {
            String v = p.getProperty(o.key);
            if (v != null) {
                o.value = Boolean.parseBoolean(v.trim());
            }
        }
        fastPlace = Boolean.parseBoolean(p.getProperty("fastPlace", "false").trim());
        try {
            int d = Integer.parseInt(p.getProperty("fastPlaceDelay", "2").trim());
            fastPlaceDelay = Math.max(1, Math.min(3, d));
        } catch (NumberFormatException ignored) {
        }
        autoSprint = Boolean.parseBoolean(p.getProperty("autoSprint", "false").trim());
        aspectEnabled = Boolean.parseBoolean(p.getProperty("aspectEnabled", "false").trim());
        hitDistance = Boolean.parseBoolean(p.getProperty("hitDistance", "true").trim());
        try {
            float a = Float.parseFloat(p.getProperty("aspectRatio", "1.33").trim());
            aspectRatio = Math.max(ASPECT_MIN, Math.min(ASPECT_MAX, a));
        } catch (NumberFormatException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Option o : OPTIONS) {
            p.setProperty(o.key, Boolean.toString(o.value));
        }
        p.setProperty("autoSprint", Boolean.toString(autoSprint));
        p.setProperty("aspectEnabled", Boolean.toString(aspectEnabled));
        p.setProperty("aspectRatio", Float.toString(aspectRatio));
        p.setProperty("hitDistance", Boolean.toString(hitDistance));
        p.setProperty("fastPlace", Boolean.toString(fastPlace));
        p.setProperty("fastPlaceDelay", Integer.toString(fastPlaceDelay));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Ool Visuals: true = effect removed");
        } catch (IOException ignored) {
        }
    }

    private VisualsConfig() {
    }
}
