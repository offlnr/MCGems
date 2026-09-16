package org.offlnr.offlnrPlugin.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.offlnr.offlnrPlugin.gem.AbysmalPickaxeFactory;
import org.offlnr.offlnrPlugin.gem.CustomItemFactory;
import org.offlnr.offlnrPlugin.gem.GemArmorFactory;
import org.offlnr.offlnrPlugin.gem.GemBlockItemFactory;
import org.offlnr.offlnrPlugin.gem.GemHoeFactory;
import org.offlnr.offlnrPlugin.gem.GemPickaxeFactory;
import org.offlnr.offlnrPlugin.gem.GemType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Función para armar el inventario de /gema items con todos los ítems entregables del plugin. */
public class GemItemsMenuFactory {

    private static final int MIN_SIZE = 27;
    private static final int MAX_SIZE = 54;
    private static final int ROW_SIZE = 9;
    private static final int ITEMS_PER_ROW = 7;
    private static final int TOOLS_FIRST_SLOT = 10;
    private static final int CRYSTALS_FIRST_SLOT = 19;
    /** Los personalizados arrancan en la fila siguiente a los cristales (slot 27 = fila 3, columna 0). */
    private static final int CUSTOM_ROW_START = 27;

    private final GemHoeFactory hoeFactory;
    private final GemPickaxeFactory pickaxeFactory;
    private final AbysmalPickaxeFactory abysmalFactory;
    private final GemArmorFactory armorFactory;
    private final GemBlockItemFactory blockItemFactory;
    private final CustomItemFactory customItemFactory;

    public GemItemsMenuFactory(GemHoeFactory hoeFactory, GemPickaxeFactory pickaxeFactory,
                                AbysmalPickaxeFactory abysmalFactory, GemArmorFactory armorFactory,
                                GemBlockItemFactory blockItemFactory, CustomItemFactory customItemFactory) {
        this.hoeFactory = hoeFactory;
        this.pickaxeFactory = pickaxeFactory;
        this.abysmalFactory = abysmalFactory;
        this.armorFactory = armorFactory;
        this.blockItemFactory = blockItemFactory;
        this.customItemFactory = customItemFactory;
    }

    public Inventory build(Player viewer) {
        Map<Integer, Supplier<ItemStack>> givers = new LinkedHashMap<>();

        List<Supplier<ItemStack>> tools = List.of(
                hoeFactory::create,
                pickaxeFactory::create,
                () -> abysmalFactory.create(viewer),
                armorFactory::createHelmet,
                armorFactory::createChestplate,
                armorFactory::createLeggings,
                armorFactory::createBoots
        );
        int slot = TOOLS_FIRST_SLOT;
        for (Supplier<ItemStack> tool : tools) {
            givers.put(slot++, tool);
        }

        slot = CRYSTALS_FIRST_SLOT;
        for (GemType type : GemType.values()) {
            givers.put(slot++, () -> blockItemFactory.create(type));
        }

        // Ítems personalizados de items.yml: se leen de nuevo en cada apertura del menú,
        // así que reflejan cualquier alta/baja hecha con /gema editar o a mano en el archivo.
        List<String> customIds = customItemFactory.listIds();
        int maxCustomRows = (MAX_SIZE - CUSTOM_ROW_START) / ROW_SIZE;
        int customRows = Math.min(maxCustomRows, (customIds.size() + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
        for (int i = 0; i < customIds.size() && i < customRows * ITEMS_PER_ROW; i++) {
            String id = customIds.get(i);
            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;
            givers.put(CUSTOM_ROW_START + row * ROW_SIZE + 1 + col, () -> customItemFactory.create(id, viewer));
        }

        int size = Math.max(MIN_SIZE, CUSTOM_ROW_START + customRows * ROW_SIZE);
        GemItemsMenuHolder holder = new GemItemsMenuHolder(givers);
        Inventory inventory = Bukkit.createInventory(holder, size,
                Component.text("Ítems disponibles", NamedTextColor.LIGHT_PURPLE));
        holder.setInventory(inventory);
        for (Map.Entry<Integer, Supplier<ItemStack>> entry : givers.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().get());
        }
        return inventory;
    }
}
