package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.util.ArrayList;
import java.util.List;

/** Genera las 4 piezas de la Armadura Cristalizada, con los encantamientos configurados en {@code armadura.<pieza>.encantamientos} (sin límite de nivel). */
public class GemArmorFactory {

    private static final String NAME_TEMPLATE =
            "<#FF00DD><sprite:\"minecraft:particles\":spark_7> <gradient:#E9FF00:#FF00DD><shadow:#6E056D:0.7>"
                    + "<underlined><bold>%s</bold></underlined></shadow></gradient> <sprite:\"minecraft:particles\":spark_7>";

    private static final List<String> DEFAULT_LORE = List.of(
            "<#7d7d7d>Forjada concentrando la energía",
            "<#7d7d7d>de innumerables gemas cristalizadas."
    );

    private final ItemsConfig itemsConfig;
    private final NamespacedKey key;

    public GemArmorFactory(OfflnrPlugin plugin, ItemsConfig itemsConfig) {
        this.itemsConfig = itemsConfig;
        this.key = new NamespacedKey(plugin, "gem_armor");
    }

    public ItemStack createHelmet() {
        return create(Material.NETHERITE_HELMET, "armadura.casco", "Casco Cristalizado");
    }

    public ItemStack createChestplate() {
        return create(Material.NETHERITE_CHESTPLATE, "armadura.pechera", "Pechera Cristalizada");
    }

    public ItemStack createLeggings() {
        return create(Material.NETHERITE_LEGGINGS, "armadura.pantalones", "Pantalones Cristalizados");
    }

    public ItemStack createBoots() {
        return create(Material.NETHERITE_BOOTS, "armadura.botas", "Botas Cristalizadas");
    }

    private ItemStack create(Material material, String configKey, String defaultText) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Component defaultName = MiniMessage.miniMessage().deserialize(String.format(NAME_TEMPLATE, defaultText));
        meta.displayName(ConfigText.parse(itemsConfig.get().getString(configKey + ".nombre"), defaultName));

        List<String> loreLines = itemsConfig.get().getStringList(configKey + ".lore");
        List<Component> lore = new ArrayList<>(ConfigText.parseList(loreLines.isEmpty() ? DEFAULT_LORE : loreLines));
        lore.add(Component.empty());
        lore.addAll(ConfigEnchants.apply(meta, itemsConfig.get().getConfigurationSection(configKey + ".encantamientos")));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    /** Reconoce si el ítem es una pieza de la Armadura Cristalizada. */
    public boolean isGemArmorPiece(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        Boolean flag = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(flag);
    }

    /** Ruta en items.yml de la pieza (ej. "armadura.casco"), o null si no es una pieza reconocida. */
    public String configPath(ItemStack item) {
        if (item == null) {
            return null;
        }
        return switch (item.getType()) {
            case NETHERITE_HELMET -> "armadura.casco";
            case NETHERITE_CHESTPLATE -> "armadura.pechera";
            case NETHERITE_LEGGINGS -> "armadura.pantalones";
            case NETHERITE_BOOTS -> "armadura.botas";
            default -> null;
        };
    }
}
