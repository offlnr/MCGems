package org.offlnr.offlnrPlugin.gem;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.offlnr.offlnrPlugin.util.Roman;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Función para aplicar encantamientos configurables (clave: nivel, sin límite) desde config.yml. */
public final class ConfigEnchants {

    private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
            Map.entry("efficiency", "Eficiencia"),
            Map.entry("fortune", "Fortuna"),
            Map.entry("unbreaking", "Irrompibilidad"),
            Map.entry("mending", "Reparación"),
            Map.entry("protection", "Protección"),
            Map.entry("blast_protection", "Protección contra explosiones"),
            Map.entry("projectile_protection", "Protección contra proyectiles"),
            Map.entry("fire_protection", "Protección contra el fuego"),
            Map.entry("respiration", "Respiración"),
            Map.entry("aqua_affinity", "Afinidad Acuática"),
            Map.entry("swift_sneak", "Sigilo Rápido"),
            Map.entry("depth_strider", "Agilidad Acuática"),
            Map.entry("feather_falling", "Caída de plumas"),
            Map.entry("soul_speed", "Velocidad de almas"),
            Map.entry("sharpness", "Filo"),
            Map.entry("looting", "Botín"),
            Map.entry("knockback", "Empuje"),
            Map.entry("fire_aspect", "Aspecto ígneo")
    );

    private ConfigEnchants() {
    }

    /** Aplica cada "clave: nivel" de la sección al ítem (ignorando el tope vanilla) y devuelve el lore correspondiente. */
    public static List<Component> apply(ItemMeta meta, ConfigurationSection section) {
        List<Component> lore = new ArrayList<>();
        if (section == null) {
            return lore;
        }
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        for (String key : section.getKeys(false)) {
            Enchantment enchantment = registry.get(NamespacedKey.minecraft(key));
            if (enchantment == null) {
                continue;
            }
            int level = section.getInt(key);
            meta.addEnchant(enchantment, level, true);
            String name = DISPLAY_NAMES.getOrDefault(key, key);
            lore.add(Component.text(name + " " + Roman.toRoman(level), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        return lore;
    }
}
