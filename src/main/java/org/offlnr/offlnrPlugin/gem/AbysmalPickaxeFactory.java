package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.util.List;

/**
 * Genera la Abysmal Pickaxe. El lore se arma línea por línea desde {@code abysmal.lore}
 * en items.yml con {@link LoreTemplate}; ahí se puede escribir cualquier texto MiniMessage
 * y usar estos tokens (solos en su propia línea) para insertar contenido generado:
 * <ul>
 *   <li>{@code <encantamientos>} - una línea por cada entrada de {@code abysmal.encantamientos},
 *       formateada con {@code abysmal.formato.encantamiento}</li>
 *   <li>{@code <atributos>} - una línea por cada entrada de {@code abysmal.atributos},
 *       formateada con {@code abysmal.formato.atributo}</li>
 *   <li>{@code <creador>} - la línea de {@code abysmal.creador}, con {@code <jugador>} resuelto</li>
 * </ul>
 */
public class AbysmalPickaxeFactory {

    private static final String DEFAULT_NAME =
            "<sprite:\"minecraft:particles\":gust_7> <gradient:#6f6f6f:#cfcfcf><shadow:#b300a7:0.6>"
                    + "<bold>Abysmal Pickaxe</bold></shadow></gradient> <sprite:\"minecraft:particles\":gust_7>";

    private static final List<String> DEFAULT_LORE_TEMPLATE = List.of(
            "<encantamientos>",
            "<atributos>",
            "",
            "<creador>"
    );

    private static final String DEFAULT_ENCHANT_FORMAT =
            "<sprite:\"minecraft:particles\":spark_7> <#7d7d7d><nombre> <#ffffff><nivel>";

    private static final String DEFAULT_ATTRIBUTE_FORMAT =
            "<sprite:\"minecraft:particles\":gust_7> <#7d7d7d><nombre> <#ffffff><nivel>";

    private static final String DEFAULT_CREATOR =
            "<sprite:\"minecraft:particles\":spark_7> <#8a8a8a>Creado para <#d6d6d6><jugador>";

    private final OfflnrPlugin plugin;
    private final ItemsConfig itemsConfig;
    private final NamespacedKey key;

    public AbysmalPickaxeFactory(OfflnrPlugin plugin, ItemsConfig itemsConfig) {
        this.plugin = plugin;
        this.itemsConfig = itemsConfig;
        this.key = new NamespacedKey(plugin, "abysmal_pickaxe");
    }

    public ItemStack create(Player owner) {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        FileConfiguration config = itemsConfig.get();

        Component defaultName = MiniMessage.miniMessage().deserialize(DEFAULT_NAME);
        meta.displayName(ConfigText.parse(config.getString("abysmal.nombre"), defaultName));

        List<String> template = config.getStringList("abysmal.lore");
        if (template.isEmpty()) {
            template = DEFAULT_LORE_TEMPLATE;
        }
        meta.lore(LoreTemplate.build(plugin, config, "abysmal", meta, owner, template,
                DEFAULT_ENCHANT_FORMAT, DEFAULT_ATTRIBUTE_FORMAT, DEFAULT_CREATOR));

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isAbysmalPickaxe(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE || !item.hasItemMeta()) {
            return false;
        }
        Boolean flag = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(flag);
    }
}
