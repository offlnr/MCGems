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

/** Genera la Picota Cristalizada, con los encantamientos configurados en {@code picota.encantamientos} (sin límite de nivel). */
public class GemPickaxeFactory {

    private static final String DEFAULT_NAME =
            "<sprite:\"minecraft:particles\":gust_7> <gradient:#525251:#bababa><shadow:#b300a7:0.6>"
                    + "<underlined><bold>Picota Cristalizada</bold></underlined></shadow></gradient> "
                    + "<sprite:\"minecraft:particles\":gust_7>";

    private static final List<String> DEFAULT_LORE = List.of(
            "<#7d7d7d>Forjada concentrando la energía",
            "<#7d7d7d>de innumerables gemas cristalizadas."
    );

    private final ItemsConfig itemsConfig;
    private final NamespacedKey key;

    public GemPickaxeFactory(OfflnrPlugin plugin, ItemsConfig itemsConfig) {
        this.itemsConfig = itemsConfig;
        this.key = new NamespacedKey(plugin, "gem_pickaxe");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        Component defaultName = MiniMessage.miniMessage().deserialize(DEFAULT_NAME);
        meta.displayName(ConfigText.parse(itemsConfig.get().getString("picota.nombre"), defaultName));

        List<String> loreLines = itemsConfig.get().getStringList("picota.lore");
        List<Component> lore = new ArrayList<>(ConfigText.parseList(loreLines.isEmpty() ? DEFAULT_LORE : loreLines));
        lore.add(Component.empty());
        lore.addAll(ConfigEnchants.apply(meta, itemsConfig.get().getConfigurationSection("picota.encantamientos")));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isGemPickaxe(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE || !item.hasItemMeta()) {
            return false;
        }
        Boolean flag = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(flag);
    }
}
