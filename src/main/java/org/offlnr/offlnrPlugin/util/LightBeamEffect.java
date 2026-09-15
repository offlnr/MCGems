package org.offlnr.offlnrPlugin.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/** Función para enganchar un haz de partículas (estilo faro) a un ítem tirado en el mundo. */
public final class LightBeamEffect {

    private static final double BEAM_HEIGHT = 20.0;
    private static final double PARTICLE_STEP = 0.25;

    private LightBeamEffect() {
    }

    public static void attach(Plugin plugin, Item item, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);
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
                    world.spawnParticle(Particle.DUST, x, y + offset, z, 1, 0, 0, 0, dust);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
