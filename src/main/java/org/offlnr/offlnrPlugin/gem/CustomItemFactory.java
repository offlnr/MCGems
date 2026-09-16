package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Función para crear ítems totalmente personalizados, definidos en items.yml bajo
 * "personalizados.<id>". Además del lore plano (nombre/lore/encantamientos, como el
 * resto de los ítems), admite la misma plantilla con tokens que la Abysmal Pickaxe
 * ({@code <encantamientos>}/{@code <atributos>}/{@code <creador>}, ver {@link LoreTemplate})
 * si "lore" usa alguno de esos tokens.
 */
public class CustomItemFactory {

    private static final String BASE_PATH = "personalizados.";

    private static final String DEFAULT_ENCHANT_FORMAT = "<#7d7d7d><nombre> <#ffffff><nivel>";
    private static final String DEFAULT_ATTRIBUTE_FORMAT = "<#7d7d7d><nombre> <#ffffff><nivel>";
    private static final String DEFAULT_CREATOR = "<#8a8a8a>Creado para <#d6d6d6><jugador>";

    private final Plugin plugin;
    private final ItemsConfig itemsConfig;

    public CustomItemFactory(Plugin plugin, ItemsConfig itemsConfig) {
        this.plugin = plugin;
        this.itemsConfig = itemsConfig;
    }

    public boolean exists(String id) {
        return itemsConfig.get().isConfigurationSection(BASE_PATH + id);
    }

    /** Guarda una copia del ítem (material, nombre, lore y encantamientos) como "personalizados.<id>". */
    public void createEntryFromItem(String id, ItemStack item) {
        String path = BASE_PATH + id;
        FileConfiguration config = itemsConfig.get();
        config.set(path, null);
        config.set(path + ".material", item.getType().name());

        ItemMeta meta = item.getItemMeta();
        MiniMessage miniMessage = MiniMessage.miniMessage();

        config.set(path + ".nombre", meta.hasDisplayName() ? miniMessage.serialize(meta.displayName()) : "");

        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            for (Component line : meta.lore()) {
                lore.add(miniMessage.serialize(line));
            }
        }
        config.set(path + ".lore", lore);

        if (meta.hasEnchants()) {
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                config.set(path + ".encantamientos." + entry.getKey().getKey().getKey(), entry.getValue());
            }
        }

        // Deja armada la estructura completa de opciones editables (aunque queden vacías),
        // para que se vean directo en items.yml sin tener que recordar el formato.
        ensureSection(config, path + ".encantamientos");
        ensureSection(config, path + ".atributos");
        config.set(path + ".formato.encantamiento", "");
        config.set(path + ".formato.atributo", "");
        config.set(path + ".creador", "");

        itemsConfig.save();
    }

    private void ensureSection(FileConfiguration config, String path) {
        if (config.getConfigurationSection(path) == null) {
            config.createSection(path);
        }
    }

    public void delete(String id) {
        itemsConfig.get().set(BASE_PATH + id, null);
        itemsConfig.save();
    }

    public List<String> listIds() {
        ConfigurationSection section = itemsConfig.get().getConfigurationSection("personalizados");
        return section == null ? List.of() : new ArrayList<>(section.getKeys(false));
    }

    public ItemStack create(String id, Player owner) {
        String path = BASE_PATH + id;
        FileConfiguration config = itemsConfig.get();

        Material material = Material.matchMaterial(config.getString(path + ".material", "STONE"));
        if (material == null) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String nombre = config.getString(path + ".nombre");
        if (nombre != null && !nombre.isBlank()) {
            meta.displayName(ConfigText.parse(nombre, Component.text(id)));
        }

        List<String> loreLines = config.getStringList(path + ".lore");
        List<Component> lore;
        if (LoreTemplate.usesTokens(loreLines)) {
            lore = LoreTemplate.build(plugin, config, path, meta, owner, loreLines,
                    DEFAULT_ENCHANT_FORMAT, DEFAULT_ATTRIBUTE_FORMAT, DEFAULT_CREATOR);
        } else {
            lore = new ArrayList<>(ConfigText.parseList(loreLines));
            lore.addAll(ConfigEnchants.apply(meta, config.getConfigurationSection(path + ".encantamientos")));
        }
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}
