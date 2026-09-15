package org.offlnr.offlnrPlugin.gem;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/** Registro persistente de los bloques de gema del mundo. */
public class GemBlockRegistry {

    private final OfflnrPlugin plugin;
    private final File file;
    private final Map<GemLocation, GemType> gems = new HashMap<>();

    public GemBlockRegistry(OfflnrPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "gemas.yml");
    }

    public void load() {
        gems.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("gemas");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            String world = entry.getString("world");
            GemType type = GemType.fromId(entry.getString("type"));
            if (world == null || type == null) {
                continue;
            }
            GemLocation loc = new GemLocation(world, entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
            gems.put(loc, type);
        }
        plugin.getLogger().info("Cargadas " + gems.size() + " gemas desde gemas.yml");
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        int i = 0;
        for (Map.Entry<GemLocation, GemType> entry : gems.entrySet()) {
            String path = "gemas." + (i++);
            GemLocation loc = entry.getKey();
            yaml.set(path + ".world", loc.world());
            yaml.set(path + ".x", loc.x());
            yaml.set(path + ".y", loc.y());
            yaml.set(path + ".z", loc.z());
            yaml.set(path + ".type", entry.getValue().name());
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar gemas.yml", e);
        }
    }

    /** Coloca el bloque en el mundo y lo registra como gema. */
    public GemType register(Block block, GemType type) {
        GemType previous = registerWithoutSaving(block, type);
        save();
        return previous;
    }

    /** Igual que register, pero sin guardar a disco (para altas masivas). */
    public GemType registerWithoutSaving(Block block, GemType type) {
        block.setType(type.blockMaterial());
        return gems.put(GemLocation.of(block), type);
    }

    /** Solo quita el registro; no toca el bloque en el mundo. */
    public GemType unregister(Block block) {
        GemType removed = gems.remove(GemLocation.of(block));
        if (removed != null) {
            save();
        }
        return removed;
    }

    public GemType getType(Block block) {
        return gems.get(GemLocation.of(block));
    }

    public boolean isGemBlock(Block block) {
        return gems.containsKey(GemLocation.of(block));
    }

    /** Igual que isGemBlock, pero sin necesitar el mundo cargado. */
    public boolean isGemLocation(GemLocation location) {
        return gems.containsKey(location);
    }

    public int size() {
        return gems.size();
    }
}
