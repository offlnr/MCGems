package org.offlnr.offlnrPlugin.gem;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Posición de un bloque de gema, guardada por coordenadas en vez de por
 * referencia directa a {@link Block}/{@link World} para que sea segura de
 * serializar y de mantener en memoria aunque el mundo se descargue.
 */
public record GemLocation(String world, int x, int y, int z) {

    public static GemLocation of(Block block) {
        return new GemLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public Block toBlock() {
        World w = Bukkit.getWorld(world);
        return w == null ? null : w.getBlockAt(x, y, z);
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        return w == null ? null : new Location(w, x, y, z);
    }
}
