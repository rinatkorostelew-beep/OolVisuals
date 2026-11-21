package dev.ool.visuals;

import net.fabricmc.loader.api.FabricLoader;

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
    public static final Option ALL_PARTICLES = new Option("noParticles", "Все частицы (и криты тоже)", false);

    public static final List<Option> OPTIONS = List.of(
            SHAKE, TOTEM, FOG, BLOCK_OVERLAY, ARROWS, BREAK_PARTICLES, ALL_PARTICLES);

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
    }

    public static void save() {
        Properties p = new Properties();
        for (Option o : OPTIONS) {
            p.setProperty(o.key, Boolean.toString(o.value));
        }
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Ool Visuals: true = effect removed");
        } catch (IOException ignored) {
        }
    }

    private VisualsConfig() {
    }
}
