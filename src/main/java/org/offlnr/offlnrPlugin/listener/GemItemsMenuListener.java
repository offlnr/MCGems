package org.offlnr.offlnrPlugin.listener;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.offlnr.offlnrPlugin.menu.GemItemsMenuHolder;

import java.util.function.Supplier;

/** Función para entregar el ítem clickeado en el menú de /gema items, con sonido y partículas. */
public class GemItemsMenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GemItemsMenuHolder holder)
                || event.getClickedInventory() != event.getInventory()) {
            return;
        }
        event.setCancelled(true);

        Supplier<ItemStack> giver = holder.giverAt(event.getSlot());
        if (giver == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        player.getInventory().addItem(giver.get());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        player.getWorld().spawnParticle(Particle.DUST, player.getEyeLocation(), 20,
                0.4, 0.4, 0.4, new Particle.DustOptions(Color.fromRGB(0xFF7AEE), 1.2f));
    }
}
