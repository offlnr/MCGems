package org.offlnr.offlnrPlugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.offlnr.offlnrPlugin.gem.ConfigText;

/** Función de prueba: previsualiza en el chat un texto en formato MiniMessage. */
public class MiniMessageCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Uso: /mm <texto con tags de MiniMessage>", NamedTextColor.RED));
            return true;
        }
        String raw = String.join(" ", args);
        Component preview = ConfigText.parse(raw, Component.empty());
        sender.sendMessage(Component.text("» ", NamedTextColor.DARK_GRAY).append(preview));
        return true;
    }
}
