package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Genera la Abysmal Pickaxe. El lore se arma línea por línea desde {@code abysmal.lore}
 * en config.yml; ahí se puede escribir cualquier texto MiniMessage y usar estos tokens
 * (solos en su propia línea) para insertar contenido generado:
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
    private final NamespacedKey key;

    public AbysmalPickaxeFactory(OfflnrPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "abysmal_pickaxe");
    }

    public ItemStack create(Player owner) {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        FileConfiguration config = plugin.getConfig();

        Component defaultName = MiniMessage.miniMessage().deserialize(DEFAULT_NAME);
        meta.displayName(ConfigText.parse(config.getString("abysmal.nombre"), defaultName));

        List<LoreEntry> enchantEntries =
                ConfigEnchants.collect(meta, config.getConfigurationSection("abysmal.encantamientos"));
        List<LoreEntry> attributeEntries =
                ConfigAttributes.apply(meta, config.getConfigurationSection("abysmal.atributos"), plugin);

        String enchantFormat = orDefault(config.getString("abysmal.formato.encantamiento"), DEFAULT_ENCHANT_FORMAT);
        String attributeFormat = orDefault(config.getString("abysmal.formato.atributo"), DEFAULT_ATTRIBUTE_FORMAT);
        List<Component> enchantLines = formatEntries(enchantEntries, enchantFormat);
        List<Component> attributeLines = formatEntries(attributeEntries, attributeFormat);

        TagResolver ownerTag = Placeholder.unparsed("jugador", owner.getName());
        Component defaultCreator = MiniMessage.miniMessage().deserialize(DEFAULT_CREATOR, ownerTag);
        Component creatorLine = ConfigText.parse(config.getString("abysmal.creador"), defaultCreator, ownerTag);

        List<String> template = config.getStringList("abysmal.lore");
        if (template.isEmpty()) {
            template = DEFAULT_LORE_TEMPLATE;
        }

        List<Component> lore = new ArrayList<>();
        for (String rawLine : template) {
            switch (rawLine.trim()) {
                case "<encantamientos>" -> lore.addAll(enchantLines);
                case "<atributos>" -> lore.addAll(attributeLines);
                case "<creador>" -> lore.add(creatorLine);
                default -> lore.add(ConfigText.parse(rawLine, Component.empty(), ownerTag));
            }
        }
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    private static List<Component> formatEntries(List<LoreEntry> entries, String template) {
        List<Component> lines = new ArrayList<>();
        for (LoreEntry entry : entries) {
            TagResolver resolver = TagResolver.resolver(
                    Placeholder.unparsed("nombre", entry.name()),
                    Placeholder.unparsed("nivel", entry.level()));
            lines.add(ConfigText.parse(template, Component.empty(), resolver));
        }
        return lines;
    }

    private static String orDefault(String raw, String fallback) {
        return (raw == null || raw.isBlank()) ? fallback : raw;
    }
}
