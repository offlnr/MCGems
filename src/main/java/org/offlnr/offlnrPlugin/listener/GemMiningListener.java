package org.offlnr.offlnrPlugin.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.offlnr.offlnrPlugin.gem.GemBlockItemFactory;
import org.offlnr.offlnrPlugin.gem.GemBlockRegistry;
import org.offlnr.offlnrPlugin.gem.GemHeadFactory;
import org.offlnr.offlnrPlugin.gem.GemHoeFactory;
import org.offlnr.offlnrPlugin.gem.GemLocation;
import org.offlnr.offlnrPlugin.gem.GemMineManager;
import org.offlnr.offlnrPlugin.gem.GemType;

import java.util.Map;

/**
 * Controla quién puede tocar los bloques de gema.
 *
 * <p>La lentitud del picado (30s) NO se simula acá: la declara la propia
 * Azada de Gemas vía {@code ToolComponent} (ver {@link GemHoeFactory}), así
 * que Minecraft calcula la rotura de forma 100% vanilla y la animación de
 * grietas es la nativa, sin parpadeos. Acá solo hace falta: (1) que ningún
 * otro ítem pueda ni rasguñar el bloque, y (2) reemplazar el drop cuando la
 * azada correcta termina de romperlo de verdad.</p>
 */
public class GemMiningListener implements Listener {

    private final GemBlockRegistry registry;
    private final GemHoeFactory hoeFactory;
    private final GemHeadFactory headFactory;
    private final GemBlockItemFactory blockItemFactory;
    private final GemMineManager mineManager;

    public GemMiningListener(GemBlockRegistry registry, GemHoeFactory hoeFactory, GemHeadFactory headFactory,
                              GemBlockItemFactory blockItemFactory, GemMineManager mineManager) {
        this.registry = registry;
        this.hoeFactory = hoeFactory;
        this.headFactory = headFactory;
        this.blockItemFactory = blockItemFactory;
        this.mineManager = mineManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        GemType type = blockItemFactory.readType(event.getItemInHand());
        if (type == null) {
            return;
        }
        // El bloque ya se plantó con el material correcto (es el mismo
        // vidrio teñido); solo hace falta registrarlo como gema minable.
        registry.register(event.getBlockPlaced(), type);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        if (!registry.isGemBlock(block)) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!hoeFactory.isGemHoe(tool)) {
            event.setCancelled(true);
            // Fuerza la grieta de vuelta a "nada" ya mismo, para que no
            // quede ni un frame de animación visible mientras llega la
            // corrección del servidor.
            player.sendBlockDamage(block.getLocation(), 0f);
            player.sendActionBar(Component.text("Solo la Azada de Gemas puede extraer esto.", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        GemType type = registry.getType(block);
        if (type == null) {
            return;
        }

        // Red de seguridad: no debería llegar hasta acá sin la Azada de
        // Gemas (se frena antes en onDamage), pero por si otro plugin fuerza
        // la rotura (ej: block.breakNaturally()) sin pasar por ese evento.
        Player player = event.getPlayer();
        if (!hoeFactory.isGemHoe(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            return;
        }

        // Si llegó hasta acá es porque la rotura la completó la Azada de
        // Gemas. Se reemplaza el drop de vidrio por la cabeza custom, que
        // va directo al inventario en vez de tirarse al piso.
        event.setDropItems(false);
        GemLocation location = GemLocation.of(block);
        registry.unregister(block);
        // Si este bloque pertenecía a una mina y era el último, la mina se
        // programa para regenerarse sola (ver GemMineManager).
        mineManager.onGemMined(location);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(headFactory.create(type));
        // Si el inventario está lleno, lo que no entró se tira al piso como
        // vanilla haría, para no perder el ítem.
        for (ItemStack overflow : leftover.values()) {
            player.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), overflow);
        }

        player.getWorld().playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1f, 1f);
        player.getWorld().spawnParticle(Particle.DUST, block.getLocation().toCenterLocation(), 25,
                0.3, 0.3, 0.3, new Particle.DustOptions(Color.fromRGB(type.color().value()), 1.3f));

        player.sendMessage(Component.text("¡Has extraído una ", NamedTextColor.GREEN)
                .append(Component.text(type.displayName(), type.color()))
                .append(Component.text("!", NamedTextColor.GREEN)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(registry::isGemBlock);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(registry::isGemBlock);
    }
}
