package dev.ool.visuals;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/** Remembers from how far (in blocks) you last hit a player. */
public final class HitDistance {

    private static double last = -1.0;
    private static long time = 0L;
    private static String displayText = "";

    public static void record(Player attacker, Entity target) {
        Vec3 eye = attacker.getEyePosition();
        AABB box = target.getBoundingBox();
        // closest point of the target's hitbox to your eyes, same idea as the game's reach check
        double cx = Math.max(box.minX, Math.min(eye.x, box.maxX));
        double cy = Math.max(box.minY, Math.min(eye.y, box.maxY));
        double cz = Math.max(box.minZ, Math.min(eye.z, box.maxZ));
        double dx = cx - eye.x;
        double dy = cy - eye.y;
        double dz = cz - eye.z;
        last = Math.sqrt(dx * dx + dy * dy + dz * dz);
        time = System.currentTimeMillis();
        displayText = "Удар с " + String.format(Locale.ROOT, "%.2f", last) + " бл.";
    }

    /** distance of the last hit, or -1 if there is nothing to show (none yet / older than 6 s). */
    public static double current() {
        if (last < 0 || System.currentTimeMillis() - time > 6000L) {
            return -1.0;
        }
        return last;
    }

    /** Preformatted once per hit instead of allocating/formatting a string every HUD frame. */
    public static String displayText() {
        return displayText;
    }

    private HitDistance() {
    }
}
