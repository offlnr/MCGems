package org.offlnr.offlnrPlugin.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Supplier;

/** Función para asociar cada slot del menú de /gema items con el ítem que entrega al clickearlo. */
public class GemItemsMenuHolder implements InventoryHolder {

    private final Map<Integer, Supplier<ItemStack>> givers;
    private Inventory inventory;

    public GemItemsMenuHolder(Map<Integer, Supplier<ItemStack>> givers) {
        this.givers = givers;
    }

    public Supplier<ItemStack> giverAt(int slot) {
        return givers.get(slot);
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
