package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.util.List;

/**
 * Crea y reconoce el "cristal de gema" como ítem colocable: se planta como
 * un bloque normal y, al tocar el suelo, el listener lo registra solo en el
 * {@link GemBlockRegistry} — no hace falta usar /gema colocar sobre un
 * bloque ya existente.
 */
public class GemBlockItemFactory {

    private final NamespacedKey key;

    public GemBlockItemFactory(OfflnrPlugin plugin) {
        this.key = new NamespacedKey(plugin, "gem_block_item");
    }

    public ItemStack create(GemType type) {
        ItemStack item = new ItemStack(type.blockMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Cristal de " + type.displayName(), type.color())
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Colocalo como un bloque normal para", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("crear una gema minable.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    /** Devuelve el color de gema si el ítem es un cristal colocable, o null si es un bloque normal. */
    public GemType readType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String raw = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return raw == null ? null : GemType.valueOf(raw);
    }
}
