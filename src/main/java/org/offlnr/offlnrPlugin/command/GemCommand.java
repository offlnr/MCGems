package org.offlnr.offlnrPlugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.offlnr.offlnrPlugin.gem.GemArmorFactory;
import org.offlnr.offlnrPlugin.gem.GemBlockItemFactory;
import org.offlnr.offlnrPlugin.gem.GemHoeFactory;
import org.offlnr.offlnrPlugin.gem.GemMine;
import org.offlnr.offlnrPlugin.gem.GemMineManager;
import org.offlnr.offlnrPlugin.gem.GemPickaxeFactory;
import org.offlnr.offlnrPlugin.gem.GemType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Función de administración: entrega ítems y gestiona minas vía /gema. */
public class GemCommand implements CommandExecutor, TabCompleter {

    private static final long MAX_FILL_VOLUME = 10_000;

    private static final List<String> SUBCOMMANDS =
            List.of("azada", "picota", "armadura", "cristal", "crearmina", "minas", "borrarmina");
    private static final List<String> COLOR_ARG_SUBCOMMANDS =
            List.of("cristal", "crystal", "crearmina", "createmine");

    private final GemHoeFactory hoeFactory;
    private final GemPickaxeFactory pickaxeFactory;
    private final GemArmorFactory armorFactory;
    private final GemBlockItemFactory blockItemFactory;
    private final GemMineManager mineManager;

    public GemCommand(GemHoeFactory hoeFactory, GemPickaxeFactory pickaxeFactory, GemArmorFactory armorFactory,
                       GemBlockItemFactory blockItemFactory, GemMineManager mineManager) {
        this.hoeFactory = hoeFactory;
        this.pickaxeFactory = pickaxeFactory;
        this.armorFactory = armorFactory;
        this.blockItemFactory = blockItemFactory;
        this.mineManager = mineManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "azada", "hoe" -> giveHoe(sender);
            case "picota", "pickaxe" -> givePickaxe(sender);
            case "armadura", "armor" -> giveArmor(sender);
            case "cristal", "crystal" -> giveCrystal(sender, args);
            case "crearmina", "createmine" -> createMine(sender, args);
            case "minas", "mines" -> listMines(sender);
            case "borrarmina", "deletemine" -> deleteMine(sender, args);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void giveHoe(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede recibir la azada.");
            return;
        }
        player.getInventory().addItem(hoeFactory.create());
        player.sendMessage(Component.text("Recibiste la Azada de Gemas.", NamedTextColor.LIGHT_PURPLE));
    }

    private void givePickaxe(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede recibir la picota.");
            return;
        }
        player.getInventory().addItem(pickaxeFactory.create());
        player.sendMessage(Component.text("Recibiste la Picota Cristalizada.", NamedTextColor.LIGHT_PURPLE));
    }

    private void giveArmor(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede recibir la armadura.");
            return;
        }
        player.getInventory().addItem(armorFactory.createHelmet(), armorFactory.createChestplate(),
                armorFactory.createLeggings(), armorFactory.createBoots());
        player.sendMessage(Component.text("Recibiste la Armadura Cristalizada.", NamedTextColor.LIGHT_PURPLE));
    }

    private void giveCrystal(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede recibir el cristal.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Component.text("Uso: /gema cristal <" + colorList() + ">", NamedTextColor.RED));
            return;
        }
        GemType type = GemType.fromId(args[1]);
        if (type == null) {
            player.sendMessage(Component.text("Color inválido. Usa: " + colorList() + ".", NamedTextColor.RED));
            return;
        }
        player.getInventory().addItem(blockItemFactory.create(type));
        player.sendMessage(Component.text("Recibiste un cristal de ", NamedTextColor.GREEN)
                .append(Component.text(type.displayName(), type.color()))
                .append(Component.text(". Colocalo como un bloque normal.", NamedTextColor.GREEN)));
    }

    private void createMine(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede crear una mina.");
            return;
        }
        if (args.length < 8) {
            player.sendMessage(Component.text(
                    "Uso: /gema crearmina <" + colorList() + "> <x1> <y1> <z1> <x2> <y2> <z2>", NamedTextColor.RED));
            return;
        }
        GemType type = GemType.fromId(args[1]);
        if (type == null) {
            player.sendMessage(Component.text("Color inválido. Usa: " + colorList() + ".", NamedTextColor.RED));
            return;
        }

        int x1, y1, z1, x2, y2, z2;
        try {
            x1 = Integer.parseInt(args[2]);
            y1 = Integer.parseInt(args[3]);
            z1 = Integer.parseInt(args[4]);
            x2 = Integer.parseInt(args[5]);
            y2 = Integer.parseInt(args[6]);
            z2 = Integer.parseInt(args[7]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Las coordenadas tienen que ser números enteros.", NamedTextColor.RED));
            return;
        }

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        long volume = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (volume > MAX_FILL_VOLUME) {
            player.sendMessage(Component.text(
                    "Esa región tiene " + volume + " bloques; el máximo permitido de una sola vez es "
                            + MAX_FILL_VOLUME + ".", NamedTextColor.RED));
            return;
        }

        World world = player.getWorld();
        List<Block> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }

        GemMine mine = mineManager.createMine(blocks, type);
        player.sendMessage(Component.text("Mina '" + mine.id() + "' creada con " + blocks.size() + " gemas de ", NamedTextColor.GREEN)
                .append(Component.text(type.displayName(), type.color()))
                .append(Component.text(". Se regenera sola cuando se agota del todo.", NamedTextColor.GREEN)));
    }

    private void listMines(CommandSender sender) {
        if (mineManager.mines().isEmpty()) {
            sender.sendMessage(Component.text("No hay minas creadas.", NamedTextColor.GRAY));
            return;
        }
        sender.sendMessage(Component.text("Minas:", NamedTextColor.GOLD));
        for (GemMine mine : mineManager.mines().values()) {
            sender.sendMessage(Component.text("- " + mine.id() + " (", NamedTextColor.GRAY)
                    .append(Component.text(mine.type().displayName(), mine.type().color()))
                    .append(Component.text(", " + mine.locations().size() + " bloques)", NamedTextColor.GRAY)));
        }
    }

    private void deleteMine(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /gema borrarmina <id>", NamedTextColor.RED));
            return;
        }
        boolean removed = mineManager.removeMine(args[1]);
        sender.sendMessage(removed
                ? Component.text("Mina eliminada del registro (los bloques ya puestos no se tocan).", NamedTextColor.YELLOW)
                : Component.text("No existe una mina con ese id.", NamedTextColor.RED));
    }

    private String colorList() {
        return Arrays.stream(GemType.values())
                .map(GemType::id)
                .collect(Collectors.joining("|"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("Uso:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/gema azada - Recibe la Azada de Gemas", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema picota - Recibe la Picota Cristalizada", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema armadura - Recibe la Armadura Cristalizada completa", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema cristal <color> - Recibe un cristal colocable de ese color", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema crearmina <color> <x1> <y1> <z1> <x2> <y2> <z2> - Crea una mina que se regenera sola", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema minas - Lista las minas creadas", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema borrarmina <id> - Deja de trackear una mina (no borra los bloques)", NamedTextColor.GRAY));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && COLOR_ARG_SUBCOMMANDS.contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> ids = new ArrayList<>();
            for (GemType type : GemType.values()) {
                ids.add(type.id());
            }
            return ids.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("borrarmina") || args[0].equalsIgnoreCase("deletemine"))) {
            return mineManager.mines().keySet().stream()
                    .filter(id -> id.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
