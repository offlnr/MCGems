package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Función para armar el lore de un ítem desde una plantilla con tokens
 * {@code <encantamientos>}/{@code <atributos>}/{@code <creador>} (el mismo
 * sistema que usa la Abysmal Pickaxe), reutilizable por cualquier ítem de
 * fábrica o personalizado que quiera usarlo.
 */
public final class LoreTemplate {

    private LoreTemplate() {
    }

    /** Indica si la lista de lore usa alguno de los tokens especiales (para decidir si aplica esta plantilla). */
    public static boolean usesTokens(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.equals("<encantamientos>") || trimmed.equals("<atributos>") || trimmed.equals("<creador>")) {
                return true;
            }
        }
        return false;
    }

    public static List<Component> build(Plugin plugin, FileConfiguration config, String path, ItemMeta meta,
                                         Player owner, List<String> template, String defaultEnchantFormat,
                                         String defaultAttributeFormat, String defaultCreator) {
        List<LoreEntry> enchantEntries =
                ConfigEnchants.collect(meta, config.getConfigurationSection(path + ".encantamientos"));
        List<LoreEntry> attributeEntries =
                ConfigAttributes.apply(meta, config.getConfigurationSection(path + ".atributos"), plugin);

        String enchantFormat = orDefault(config.getString(path + ".formato.encantamiento"), defaultEnchantFormat);
        String attributeFormat = orDefault(config.getString(path + ".formato.atributo"), defaultAttributeFormat);
        List<Component> enchantLines = formatEntries(enchantEntries, enchantFormat);
        List<Component> attributeLines = formatEntries(attributeEntries, attributeFormat);

        TagResolver ownerTag = Placeholder.unparsed("jugador", owner == null ? "" : owner.getName());
        Component defaultCreatorLine = MiniMessage.miniMessage().deserialize(defaultCreator, ownerTag);
        Component creatorLine = ConfigText.parse(config.getString(path + ".creador"), defaultCreatorLine, ownerTag);

        List<Component> lore = new ArrayList<>();
        for (String rawLine : template) {
            switch (rawLine.trim()) {
                case "<encantamientos>" -> lore.addAll(enchantLines);
                case "<atributos>" -> lore.addAll(attributeLines);
                case "<creador>" -> lore.add(creatorLine);
                default -> lore.add(ConfigText.parse(rawLine, Component.empty(), ownerTag));
            }
        }
        return lore;
    }

    private static List<Component> formatEntries(List<LoreEntry> entries, String template) {
        List<Component> lines = new ArrayList<>();
        for (LoreEntry entry : entries) {
            TagResolver resolver = TagResolver.resolver(
                    Placeholder.component("nombre", entry.name()),
                    Placeholder.unparsed("nivel", entry.level()));
            lines.add(ConfigText.parse(template, Component.empty(), resolver));
        }
        return lines;
    }

    private static String orDefault(String raw, String fallback) {
        return (raw == null || raw.isBlank()) ? fallback : raw;
    }
}
