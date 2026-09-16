package org.offlnr.offlnrPlugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.offlnr.offlnrPlugin.gem.AbysmalPickaxeFactory;
import org.offlnr.offlnrPlugin.gem.ConfigEnchants;
import org.offlnr.offlnrPlugin.gem.CustomItemFactory;
import org.offlnr.offlnrPlugin.gem.GemArmorFactory;
import org.offlnr.offlnrPlugin.gem.GemBlockItemFactory;
import org.offlnr.offlnrPlugin.gem.GemHoeFactory;
import org.offlnr.offlnrPlugin.gem.GemMine;
import org.offlnr.offlnrPlugin.gem.GemMineManager;
import org.offlnr.offlnrPlugin.gem.GemPickaxeFactory;
import org.offlnr.offlnrPlugin.gem.GemType;
import org.offlnr.offlnrPlugin.gem.ItemsConfig;
import org.offlnr.offlnrPlugin.menu.GemItemsMenuFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Función de administración: entrega ítems, los edita/crea, y gestiona minas vía /gema. */
public class GemCommand implements CommandExecutor, TabCompleter {

    private static final long MAX_FILL_VOLUME = 10_000;

    private static final List<String> SUBCOMMANDS =
            List.of("azada", "items", "editar", "cristal", "crearmina", "minas", "borrarmina");
    private static final List<String> COLOR_ARG_SUBCOMMANDS =
            List.of("cristal", "crystal", "crearmina", "createmine");
    private static final List<String> EDITAR_ACCIONES =
            List.of("crear", "borrar", "listar", "dar", "nombre", "lore", "encantar", "desencantar", "recargar");
    private static final List<String> LORE_ACCIONES = List.of("agregar", "quitar", "limpiar");

    /** Ítems del sistema que ya existen de fábrica; el resto de los ids son "personalizados". */
    private static final Set<String> BUILTIN_IDS =
            Set.of("azada", "picota", "abysmal", "casco", "pechera", "pantalones", "botas");

    private final GemHoeFactory hoeFactory;
    private final GemPickaxeFactory pickaxeFactory;
    private final AbysmalPickaxeFactory abysmalFactory;
    private final GemArmorFactory armorFactory;
    private final GemBlockItemFactory blockItemFactory;
    private final GemMineManager mineManager;
    private final ItemsConfig itemsConfig;
    private final CustomItemFactory customItemFactory;
    private final GemItemsMenuFactory itemsMenuFactory;

    public GemCommand(GemHoeFactory hoeFactory, GemPickaxeFactory pickaxeFactory, AbysmalPickaxeFactory abysmalFactory,
                       GemArmorFactory armorFactory, GemBlockItemFactory blockItemFactory, GemMineManager mineManager,
                       ItemsConfig itemsConfig, CustomItemFactory customItemFactory) {
        this.hoeFactory = hoeFactory;
        this.pickaxeFactory = pickaxeFactory;
        this.abysmalFactory = abysmalFactory;
        this.armorFactory = armorFactory;
        this.blockItemFactory = blockItemFactory;
        this.mineManager = mineManager;
        this.itemsConfig = itemsConfig;
        this.customItemFactory = customItemFactory;
        this.itemsMenuFactory = new GemItemsMenuFactory(
                hoeFactory, pickaxeFactory, abysmalFactory, armorFactory, blockItemFactory, customItemFactory);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "azada", "hoe" -> giveHoe(sender);
            case "items" -> openItemsMenu(sender);
            case "editar" -> handleEditar(sender, args);
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

    private void openItemsMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede abrir el menú de ítems.");
            return;
        }
        player.openInventory(itemsMenuFactory.build(player));
    }

    // ---- /gema editar ... ----

    private void handleEditar(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendEditarUsage(sender);
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "crear" -> createCustomItem(sender, args);
            case "borrar" -> deleteCustomItem(sender, args);
            case "listar" -> listEditableItems(sender);
            case "dar" -> giveEditableItem(sender, args);
            case "nombre" -> editName(sender, args);
            case "lore" -> editLore(sender, args);
            case "encantar" -> editEnchant(sender, args);
            case "desencantar" -> removeEnchant(sender, args);
            case "recargar" -> reloadItems(sender);
            default -> sendEditarUsage(sender);
        }
    }

    private void reloadItems(CommandSender sender) {
        itemsConfig.load();
        sender.sendMessage(Component.text("items.yml recargado desde disco.", NamedTextColor.GREEN));
    }

    private void createCustomItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede crear un ítem.");
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            player.sendMessage(Component.text("Tenés que tener el ítem en la mano para guardarlo.", NamedTextColor.RED));
            return;
        }
        String id = args.length >= 3
                ? args[2].toLowerCase(Locale.ROOT)
                : hand.getType().name().toLowerCase(Locale.ROOT);
        if (BUILTIN_IDS.contains(id) || customItemFactory.exists(id)) {
            player.sendMessage(Component.text("Ya existe un ítem con ese id.", NamedTextColor.RED));
            return;
        }
        customItemFactory.createEntryFromItem(id, hand);
        player.sendMessage(Component.text(
                "Ítem '" + id + "' guardado con las propiedades del que tenías en la mano.", NamedTextColor.GREEN));
    }

    private void deleteCustomItem(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Uso: /gema editar borrar <id>", NamedTextColor.RED));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        if (!customItemFactory.exists(id)) {
            sender.sendMessage(Component.text("No existe un ítem personalizado con ese id.", NamedTextColor.RED));
            return;
        }
        customItemFactory.delete(id);
        sender.sendMessage(Component.text("Ítem '" + id + "' eliminado.", NamedTextColor.YELLOW));
    }

    private void listEditableItems(CommandSender sender) {
        sender.sendMessage(Component.text("Ítems editables:", NamedTextColor.GOLD));
        for (String id : BUILTIN_IDS) {
            sender.sendMessage(Component.text("- " + id, NamedTextColor.GRAY));
        }
        for (String id : customItemFactory.listIds()) {
            sender.sendMessage(Component.text("- " + id + " (personalizado)", NamedTextColor.GRAY));
        }
    }

    private void giveEditableItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede recibir un ítem.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Component.text("Uso: /gema editar dar <id>", NamedTextColor.RED));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        ItemStack item = previewItem(player, id);
        if (item == null) {
            player.sendMessage(Component.text("No existe ese ítem: " + id, NamedTextColor.RED));
            return;
        }
        player.getInventory().addItem(item);
        player.sendMessage(Component.text("Recibiste '" + id + "'.", NamedTextColor.LIGHT_PURPLE));
    }

    private void editName(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Uso: /gema editar nombre <id> <texto>", NamedTextColor.RED));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        String path = resolvePath(id);
        if (path == null) {
            sender.sendMessage(Component.text("No existe ese ítem: " + id, NamedTextColor.RED));
            return;
        }
        String texto = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        itemsConfig.get().set(path + ".nombre", texto);
        itemsConfig.save();
        sender.sendMessage(Component.text("Nombre de '" + id + "' actualizado.", NamedTextColor.GREEN));
    }

    private void editLore(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text(
                    "Uso: /gema editar lore <id> <agregar|quitar|limpiar> [texto|indice]", NamedTextColor.RED));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        if (id.equals("abysmal")) {
            sender.sendMessage(Component.text(
                    "El lore de abysmal usa una plantilla con tokens; editalo directo en items.yml.",
                    NamedTextColor.RED));
            return;
        }
        String path = resolvePath(id);
        if (path == null) {
            sender.sendMessage(Component.text("No existe ese ítem: " + id, NamedTextColor.RED));
            return;
        }

        List<String> lore = new ArrayList<>(itemsConfig.get().getStringList(path + ".lore"));
        switch (args[3].toLowerCase(Locale.ROOT)) {
            case "agregar" -> {
                if (args.length < 5) {
                    sender.sendMessage(Component.text("Uso: /gema editar lore <id> agregar <texto>", NamedTextColor.RED));
                    return;
                }
                lore.add(String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
            }
            case "quitar" -> {
                if (args.length < 5) {
                    sender.sendMessage(Component.text("Uso: /gema editar lore <id> quitar <indice>", NamedTextColor.RED));
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(args[4]) - 1;
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("El índice tiene que ser un número entero.", NamedTextColor.RED));
                    return;
                }
                if (index < 0 || index >= lore.size()) {
                    sender.sendMessage(Component.text("No hay una línea con ese número.", NamedTextColor.RED));
                    return;
                }
                lore.remove(index);
            }
            case "limpiar" -> lore.clear();
            default -> {
                sender.sendMessage(Component.text(
                        "Uso: /gema editar lore <id> <agregar|quitar|limpiar> [texto|indice]", NamedTextColor.RED));
                return;
            }
        }
        itemsConfig.get().set(path + ".lore", lore);
        itemsConfig.save();
        sender.sendMessage(Component.text("Lore de '" + id + "' actualizado.", NamedTextColor.GREEN));
    }

    private void editEnchant(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(Component.text("Uso: /gema editar encantar <id> <encantamiento> <nivel>", NamedTextColor.RED));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        String path = resolvePath(id);
        if (path == null) {
            sender.sendMessage(Component.text("No existe ese ítem: " + id, NamedTextColor.RED));
            return;
        }
        String enchantKey = args[3].toLowerCase(Locale.ROOT);
        if (!ConfigEnchants.isValidKey(enchantKey)) {
            sender.sendMessage(Component.text("Encantamiento inválido: " + enchantKey, NamedTextColor.RED));
            return;
        }
        int level;
        try {
            level = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("El nivel tiene que ser un número entero.", NamedTextColor.RED));
            return;
        }
        itemsConfig.get().set(path + ".encantamientos." + enchantKey, level);
        itemsConfig.save();
        sender.sendMessage(Component.text("Encantamiento actualizado en '" + id + "'.", NamedTextColor.GREEN));
    }

    private void removeEnchant(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Uso: /gema editar desencantar <id> <encantamiento>", NamedTextColor.RED));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        String path = resolvePath(id);
        if (path == null) {
            sender.sendMessage(Component.text("No existe ese ítem: " + id, NamedTextColor.RED));
            return;
        }
        String enchantKey = args[3].toLowerCase(Locale.ROOT);
        ConfigurationSection section = itemsConfig.get().getConfigurationSection(path + ".encantamientos");
        if (section == null || !section.contains(enchantKey)) {
            sender.sendMessage(Component.text("Ese ítem no tiene ese encantamiento.", NamedTextColor.RED));
            return;
        }
        section.set(enchantKey, null);
        itemsConfig.save();
        sender.sendMessage(Component.text("Encantamiento quitado de '" + id + "'.", NamedTextColor.GREEN));
    }

    /** Ruta en items.yml para un id editable, o null si no existe (ni es de fábrica ni personalizado). */
    private String resolvePath(String id) {
        return switch (id) {
            case "azada", "picota", "abysmal" -> id;
            case "casco", "pechera", "pantalones", "botas" -> "armadura." + id;
            default -> customItemFactory.exists(id) ? "personalizados." + id : null;
        };
    }

    /** Genera una copia del ítem con ese id para entregarla (dar/preview), o null si no existe. */
    private ItemStack previewItem(Player player, String id) {
        return switch (id) {
            case "azada" -> hoeFactory.create();
            case "picota" -> pickaxeFactory.create();
            case "abysmal" -> abysmalFactory.create(player);
            case "casco" -> armorFactory.createHelmet();
            case "pechera" -> armorFactory.createChestplate();
            case "pantalones" -> armorFactory.createLeggings();
            case "botas" -> armorFactory.createBoots();
            default -> customItemFactory.exists(id) ? customItemFactory.create(id, player) : null;
        };
    }

    // ---- resto de comandos ----

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
        sender.sendMessage(Component.text("/gema items - Abre un menú con todos los ítems disponibles", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar ... - Crea o personaliza ítems (nombre, lore, encantamientos)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema cristal <color> - Recibe un cristal colocable de ese color", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema crearmina <color> <x1> <y1> <z1> <x2> <y2> <z2> - Crea una mina que se regenera sola", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema minas - Lista las minas creadas", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema borrarmina <id> - Deja de trackear una mina (no borra los bloques)", NamedTextColor.GRAY));
    }

    private void sendEditarUsage(CommandSender sender) {
        sender.sendMessage(Component.text("Uso de /gema editar:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/gema editar crear [id] - Guarda el ítem que tenés en la mano (si omitís el id, usa el material del ítem)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar borrar <id> - Elimina un ítem personalizado", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar listar - Lista los ids de todos los ítems editables", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar dar <id> - Te da una copia del ítem", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar nombre <id> <texto> - Cambia el nombre (MiniMessage)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar lore <id> <agregar|quitar|limpiar> [texto|indice] - Edita el lore", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar encantar <id> <encantamiento> <nivel> - Agrega o cambia un encantamiento", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar desencantar <id> <encantamiento> - Quita un encantamiento", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/gema editar recargar - Vuelve a leer items.yml del disco (si lo editaste a mano)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Ids de fábrica: azada, picota, abysmal, casco, pechera, pantalones, botas", NamedTextColor.DARK_GRAY));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args[0].equalsIgnoreCase("editar")) {
            return tabCompleteEditar(args);
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

    private List<String> tabCompleteEditar(String[] args) {
        if (args.length == 2) {
            return EDITAR_ACCIONES.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        boolean necesitaId = List.of("borrar", "dar", "nombre", "lore", "encantar", "desencantar")
                .contains(args[1].toLowerCase(Locale.ROOT));
        if (args.length == 3 && necesitaId) {
            List<String> ids = new ArrayList<>(BUILTIN_IDS);
            ids.addAll(customItemFactory.listIds());
            return ids.stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("lore")) {
            return LORE_ACCIONES.stream()
                    .filter(s -> s.startsWith(args[3].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
