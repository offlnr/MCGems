package org.offlnr.offlnrPlugin.gem;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

/** Función para administrar las minas: grupos de gemas que se regeneran solas. */
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

    /** Llena la región con el color dado y la registra como mina nueva. */
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

    /** Si la gema minada era la última de su mina, programa la regeneración. */
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
        List<Block> restored = new ArrayList<>();
        for (GemLocation location : mine.locations()) {
            Block block = location.toBlock();
            if (block == null) {
                continue; // el mundo no está cargado; se salta ese bloque
            }
            registry.registerWithoutSaving(block, mine.type());
            restored.add(block);
        }
        registry.save();
        playRegenerationEffect(mine, restored);
    }

    /** Genera partículas y sonido al regenerar la mina. */
    private void playRegenerationEffect(GemMine mine, List<Block> restored) {
        if (restored.isEmpty()) {
            return;
        }
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(mine.type().color().value()), 1.3f);
        for (Block block : restored) {
            block.getWorld().spawnParticle(Particle.DUST, block.getLocation().toCenterLocation(), 15,
                    0.3, 0.3, 0.3, dust);
        }
        Block first = restored.get(0);
        first.getWorld().playSound(first.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.4f);
    }

    private void registerMine(GemMine mine) {
        mines.put(mine.id(), mine);
        for (GemLocation location : mine.locations()) {
            locationToMine.put(location, mine.id());
        }
    }
}
