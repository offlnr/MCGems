package org.offlnr.offlnrPlugin.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.offlnr.offlnrPlugin.OfflnrPlugin;
import org.offlnr.offlnrPlugin.gem.AbysmalPickaxeFactory;
import org.offlnr.offlnrPlugin.gem.GemArmorFactory;
import org.offlnr.offlnrPlugin.gem.GemBeamConfig;
import org.offlnr.offlnrPlugin.gem.GemPickaxeFactory;
import org.offlnr.offlnrPlugin.gem.GemType;
import org.offlnr.offlnrPlugin.gem.ItemsConfig;
import org.offlnr.offlnrPlugin.util.LightBeamEffect;

/**
 * Función para enganchar un haz de luz a la Picota, la Abysmal Pickaxe o la Armadura Cristalizada
 * al caer al piso. Se puede activar/desactivar y elegir el color desde {@code <item>.rayo} en
 * items.yml (ver {@link GemBeamConfig}).
 */
public class GemItemBeamListener implements Listener {

    private final OfflnrPlugin plugin;
    private final ItemsConfig itemsConfig;
    private final GemPickaxeFactory pickaxeFactory;
    private final AbysmalPickaxeFactory abysmalFactory;
    private final GemArmorFactory armorFactory;

    public GemItemBeamListener(OfflnrPlugin plugin, ItemsConfig itemsConfig, GemPickaxeFactory pickaxeFactory,
                               AbysmalPickaxeFactory abysmalFactory, GemArmorFactory armorFactory) {
        this.plugin = plugin;
        this.itemsConfig = itemsConfig;
        this.pickaxeFactory = pickaxeFactory;
        this.abysmalFactory = abysmalFactory;
        this.armorFactory = armorFactory;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack stack = item.getItemStack();

        String path;
        GemType defaultColor;
        if (pickaxeFactory.isGemPickaxe(stack) || abysmalFactory.isAbysmalPickaxe(stack)) {
            path = pickaxeFactory.isGemPickaxe(stack) ? "picota" : "abysmal";
            defaultColor = GemType.PURPLE;
        } else if (armorFactory.isGemArmorPiece(stack)) {
            path = armorFactory.configPath(stack);
            defaultColor = GemType.YELLOW;
        } else {
            return;
        }
        if (path == null) {
            return;
        }

        FileConfiguration config = itemsConfig.get();
        if (!GemBeamConfig.isActive(config, path, true)) {
            return;
        }
        LightBeamEffect.attach(plugin, item, GemBeamConfig.resolveColor(config, path, defaultColor));
    }
}
