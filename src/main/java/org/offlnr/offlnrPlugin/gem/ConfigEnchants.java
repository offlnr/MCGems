package org.offlnr.offlnrPlugin.gem;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.offlnr.offlnrPlugin.util.Roman;

import java.util.ArrayList;
import java.util.List;

/**
 * Función para aplicar encantamientos configurables (clave: nivel, sin límite) desde items.yml.
 * El nombre mostrado en el lore es un Component traducible ({@link Component#translatable}), así
 * que cada jugador lo ve en el idioma configurado en su propio cliente, sin depender de una
 * traducción fija en el plugin.
 */
public final class ConfigEnchants {

    private ConfigEnchants() {
    }

    /** Indica si la clave corresponde a un encantamiento vanilla real, para validar antes de guardar. */
    public static boolean isValidKey(String key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(NamespacedKey.minecraft(key)) != null;
    }

    /** Aplica cada "clave: nivel" de la sección al ítem (ignorando el tope vanilla) y devuelve el lore correspondiente en gris. */
    public static List<Component> apply(ItemMeta meta, ConfigurationSection section) {
        List<Component> lore = new ArrayList<>();
        for (LoreEntry entry : collect(meta, section)) {
            lore.add(Component.text()
                    .append(entry.name())
                    .append(Component.text(" " + entry.level()))
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .build());
        }
        return lore;
    }

    /** Aplica cada "clave: nivel" de la sección al ítem (ignorando el tope vanilla) y devuelve los datos crudos, sin formatear. */
    public static List<LoreEntry> collect(ItemMeta meta, ConfigurationSection section) {
        List<LoreEntry> entries = new ArrayList<>();
        if (section == null) {
            return entries;
        }
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        for (String key : section.getKeys(false)) {
            Enchantment enchantment = registry.get(NamespacedKey.minecraft(key));
            if (enchantment == null) {
                continue;
            }
            int level = section.getInt(key);
            meta.addEnchant(enchantment, level, true);
            // Se castea a la interfaz de Adventure (no la de Bukkit, deprecada) para el mismo método.
            Component name = Component.translatable(((Translatable) enchantment).translationKey());
            entries.add(new LoreEntry(name, Roman.toRoman(level)));
        }
        return entries;
    }
}
