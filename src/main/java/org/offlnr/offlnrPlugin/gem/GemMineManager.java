package org.offlnr.offlnrPlugin.gem;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Administra las "minas": grupos de gemas creadas de una sola vez que se
 * regeneran automáticamente cuando se agota el último bloque del grupo.
 *
 * <p>No duplica el estado de "minado/sin minar" — la fuente de verdad
 * sigue siendo {@link GemBlockRegistry}. Acá solo se guarda el layout
 * original (qué ubicaciones pertenecen a qué mina) para poder restaurarlo
 * y para saber, cuando se rompe una gema, si esa mina quedó vacía.</p>
 */
public class GemMineManager {

    private final OfflnrPlugin plugin;
    private final GemBlockRegistry registry;
    private final File file;

    private final Map<String, GemMine> mines = new HashMap<>();
    private final Map<GemLocation, String> locationToMine = new HashMap<>();

    public GemMineManager(OfflnrPlugin plugin, GemBlockRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
        this.file = new File(plugin.getDataFolder(), "minas.yml");
    }

    public void load() {
        mines.clear();
        locationToMine.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("minas");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(id);
            if (entry == null) {
                continue;
            }
            GemType type = GemType.fromId(entry.getString("tipo"));
            List<?> rawBlocks = entry.getList("bloques");
            if (type == null || rawBlocks == null) {
                continue;
            }
            List<GemLocation> locations = new ArrayList<>();
            for (Object raw : rawBlocks) {
                if (!(raw instanceof Map<?, ?> map)) {
                    continue;
                }
                String world = String.valueOf(map.get("world"));
                int x = Integer.parseInt(String.valueOf(map.get("x")));
                int y = Integer.parseInt(String.valueOf(map.get("y")));
                int z = Integer.parseInt(String.valueOf(map.get("z")));
                locations.add(new GemLocation(world, x, y, z));
            }
            registerMine(new GemMine(id, type, locations));
        }
        plugin.getLogger().info("Cargadas " + mines.size() + " minas desde minas.yml");
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (GemMine mine : mines.values()) {
            String path = "minas." + mine.id();
            yaml.set(path + ".tipo", mine.type().name());
            List<Map<String, Object>> serialized = new ArrayList<>();
            for (GemLocation loc : mine.locations()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("world", loc.world());
                entry.put("x", loc.x());
                entry.put("y", loc.y());
                entry.put("z", loc.z());
                serialized.add(entry);
            }
            yaml.set(path + ".bloques", serialized);
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar minas.yml", e);
        }
    }

    /** Llena la región (bloques ya calculados por el comando) con el color dado y la registra como mina nueva. */
    public GemMine createMine(List<Block> blocks, GemType type) {
        List<GemLocation> locations = new ArrayList<>();
        for (Block block : blocks) {
            registry.registerWithoutSaving(block, type);
            locations.add(GemLocation.of(block));
        }
        registry.save();

        String id = "mina-" + UUID.randomUUID().toString().substring(0, 8);
        GemMine mine = new GemMine(id, type, locations);
        registerMine(mine);
        save();
        return mine;
    }

    public boolean removeMine(String id) {
        GemMine mine = mines.remove(id);
        if (mine == null) {
            return false;
        }
        mine.locations().forEach(locationToMine::remove);
        save();
        return true;
    }

    public Map<String, GemMine> mines() {
        return mines;
    }

    /** Avisa que se acaba de minar una gema; si esa ubicación pertenece a una mina y era la última, la regenera. */
    public void onGemMined(GemLocation location) {
        String mineId = locationToMine.get(location);
        if (mineId == null) {
            return;
        }
        GemMine mine = mines.get(mineId);
        if (mine == null) {
            return;
        }
        boolean anyRemaining = mine.locations().stream().anyMatch(registry::isGemLocation);
        if (anyRemaining) {
            return;
        }
        long delayTicks = Math.max(0, plugin.getConfig().getLong("minas.regeneracion-segundos", 10)) * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> regenerate(mine), delayTicks);
    }

    private void regenerate(GemMine mine) {
        for (GemLocation location : mine.locations()) {
            Block block = location.toBlock();
            if (block == null) {
                continue; // el mundo no está cargado; se salta ese bloque
            }
            registry.registerWithoutSaving(block, mine.type());
        }
        registry.save();
    }

    private void registerMine(GemMine mine) {
        mines.put(mine.id(), mine);
        for (GemLocation location : mine.locations()) {
            locationToMine.put(location, mine.id());
        }
    }
}
