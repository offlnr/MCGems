package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.persistence.PersistentDataType;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.util.List;
import java.util.stream.Collectors;

/** Genera y reconoce la Azada de Gemas: durabilidad infinita y velocidad de minado configurable. */
public class GemHoeFactory {

    private static final float GLASS_HARDNESS = 0.3f;
    private static final float CORRECT_TOOL_DIVISOR = 30f;
    private static final int DEFAULT_MINING_SECONDS = 30;

    private final OfflnrPlugin plugin;
    private final NamespacedKey key;

    public GemHoeFactory(OfflnrPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "gem_hoe");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();

        Component defaultName = Component.text("Azada de Gemas", NamedTextColor.LIGHT_PURPLE);
        List<Component> defaultLore = List.of(
                Component.text("Única herramienta capaz de extraer", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("gemas cristalizadas.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.displayName(ConfigText.parse(plugin.getConfig().getString("azada.nombre"), defaultName));
        List<String> loreLines = plugin.getConfig().getStringList("azada.lore");
        meta.lore(loreLines.isEmpty() ? defaultLore : ConfigText.parseList(loreLines));

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.setEnchantmentGlintOverride(true);

        ToolComponent tool = meta.getTool();
        List<Material> gemMaterials = List.of(GemType.values()).stream()
                .map(GemType::blockMaterial)
                .collect(Collectors.toList());
        tool.addRule(gemMaterials, miningSpeed(), true);
        meta.setTool(tool);

        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    private float miningSpeed() {
        int seconds = plugin.getConfig().getInt("azada.tiempo-minado-segundos", DEFAULT_MINING_SECONDS);
        if (seconds < 1) {
            seconds = 1;
        }
        int ticks = seconds * 20;
        return (GLASS_HARDNESS * CORRECT_TOOL_DIVISOR) / ticks;
    }

    public boolean isGemHoe(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_HOE || !item.hasItemMeta()) {
            return false;
        }
        Boolean flag = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(flag);
    }
}
