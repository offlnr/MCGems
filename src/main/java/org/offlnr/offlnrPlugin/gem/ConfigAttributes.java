package org.offlnr.offlnrPlugin.gem;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.offlnr.offlnrPlugin.util.Roman;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Función para aplicar atributos configurables (activos solo con el ítem en la mano) desde config.yml. */
public final class ConfigAttributes {

    private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
            Map.entry("movement_speed", "Velocidad"),
            Map.entry("knockback_resistance", "Resistencia al empuje"),
            Map.entry("explosion_knockback_resistance", "Resistencia a explosiones"),
            Map.entry("attack_damage", "Daño de ataque"),
            Map.entry("attack_speed", "Velocidad de ataque"),
            Map.entry("attack_knockback", "Empuje de ataque"),
            Map.entry("armor", "Armadura"),
            Map.entry("armor_toughness", "Dureza de armadura"),
            Map.entry("max_health", "Vida máxima"),
            Map.entry("mining_efficiency", "Eficiencia de minado"),
            Map.entry("block_break_speed", "Velocidad de rotura"),
            Map.entry("block_interaction_range", "Alcance de bloques"),
            Map.entry("entity_interaction_range", "Alcance de entidades"),
            Map.entry("jump_strength", "Fuerza de salto"),
            Map.entry("step_height", "Altura de paso"),
            Map.entry("safe_fall_distance", "Caída segura"),
            Map.entry("fall_damage_multiplier", "Daño de caída"),
            Map.entry("scale", "Tamaño"),
            Map.entry("luck", "Suerte")
    );

    private ConfigAttributes() {
    }

    /** Aplica cada atributo de la sección al ítem y devuelve los datos crudos, sin formatear. */
    public static List<LoreEntry> apply(ItemMeta meta, ConfigurationSection section, Plugin plugin) {
        List<LoreEntry> entries = new ArrayList<>();
        if (section == null) {
            return entries;
        }
        Registry<Attribute> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE);
        for (String key : section.getKeys(false)) {
            Attribute attribute = registry.get(NamespacedKey.minecraft(key));
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (attribute == null || entry == null) {
                continue;
            }
            meta.addAttributeModifier(attribute, new AttributeModifier(
                    new NamespacedKey(plugin, "atributo_" + key),
                    entry.getDouble("cantidad"),
                    operation(entry.getString("operacion")),
                    EquipmentSlotGroup.MAINHAND));

            String name = DISPLAY_NAMES.getOrDefault(key, key);
            entries.add(new LoreEntry(name, Roman.toRoman(entry.getInt("nivel", 1))));
        }
        return entries;
    }

    private static AttributeModifier.Operation operation(String raw) {
        if (raw == null) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
        try {
            return AttributeModifier.Operation.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
    }
}
