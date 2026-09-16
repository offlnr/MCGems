package org.offlnr.offlnrPlugin.gem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/** Función para administrar items.yml: nombre, lore y encantamientos de todos los ítems del plugin. */
public class ItemsConfig {

    private final OfflnrPlugin plugin;
    private final File file;
    private FileConfiguration yaml;

    public ItemsConfig(OfflnrPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "items.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("items.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return yaml;
    }

    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar items.yml", e);
        }
    }
}
