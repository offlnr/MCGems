package org.offlnr.offlnrPlugin.listener;

import org.bukkit.Color;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.offlnr.offlnrPlugin.OfflnrPlugin;
import org.offlnr.offlnrPlugin.gem.GemArmorFactory;
import org.offlnr.offlnrPlugin.gem.GemPickaxeFactory;
import org.offlnr.offlnrPlugin.util.LightBeamEffect;

/** Función para enganchar un haz de luz a la Picota o la Armadura Cristalizada al caer al piso. */
public class GemItemBeamListener implements Listener {

    private static final Color PICKAXE_COLOR = Color.fromRGB(0x9B30FF);
    private static final Color ARMOR_COLOR = Color.fromRGB(0xFFFF00);

    private final OfflnrPlugin plugin;
    private final GemPickaxeFactory pickaxeFactory;
    private final GemArmorFactory armorFactory;

    public GemItemBeamListener(OfflnrPlugin plugin, GemPickaxeFactory pickaxeFactory, GemArmorFactory armorFactory) {
        this.plugin = plugin;
        this.pickaxeFactory = pickaxeFactory;
        this.armorFactory = armorFactory;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (pickaxeFactory.isGemPickaxe(item.getItemStack())) {
            LightBeamEffect.attach(plugin, item, PICKAXE_COLOR);
        } else if (armorFactory.isGemArmorPiece(item.getItemStack())) {
            LightBeamEffect.attach(plugin, item, ARMOR_COLOR);
        }
    }
}
