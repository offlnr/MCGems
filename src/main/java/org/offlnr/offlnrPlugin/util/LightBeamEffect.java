package org.offlnr.offlnrPlugin.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Función para enganchar un haz de partículas fino a un ítem tirado en el mundo: una sola línea
 * de partículas (sin anillos) cuyo tamaño se va achicando hasta terminar en punta arriba.
 */
public final class LightBeamEffect {

    private static final double BEAM_HEIGHT = 6.0;
    private static final double PARTICLE_STEP = 0.2;
    private static final float BASE_SIZE = 0.55f;
    private static final float TIP_SIZE = 0.1f;

    private LightBeamEffect() {
    }

    public static void attach(Plugin plugin, Item item, Color color) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isValid()) {
                    cancel();
                    return;
                }
                Location base = item.getLocation();
                World world = base.getWorld();
                double x = base.getX();
                double y = base.getY();
                double z = base.getZ();
                for (double offset = 0; offset <= BEAM_HEIGHT; offset += PARTICLE_STEP) {
                    float progress = (float) (offset / BEAM_HEIGHT);
                    float size = BASE_SIZE + (TIP_SIZE - BASE_SIZE) * progress;
                    Particle.DustOptions dust = new Particle.DustOptions(color, size);
                    world.spawnParticle(Particle.DUST, x, y + offset, z, 1, 0, 0, 0, dust);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
