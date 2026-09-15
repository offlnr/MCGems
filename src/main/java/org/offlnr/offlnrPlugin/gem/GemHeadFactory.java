package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.offlnr.offlnrPlugin.OfflnrPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/** Genera la cabeza custom que dropea cada gema. */
public class GemHeadFactory {

    private final OfflnrPlugin plugin;
    private final NamespacedKey typeKey;

    public GemHeadFactory(OfflnrPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "gem_type");
    }

    public ItemStack create(GemType type) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        String basePath = "cabezas." + type.configKey();

        Component defaultName = Component.text(type.headTitle(), NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.UNDERLINED, true);
        List<Component> defaultLore = List.of(
                Component.text("Custom Head ID: " + type.headId(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("www.minecraft-heads.com", NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.displayName(ConfigText.parse(plugin.getConfig().getString(basePath + ".nombre"), defaultName));
        List<String> loreLines = plugin.getConfig().getStringList(basePath + ".lore");
        meta.lore(loreLines.isEmpty() ? defaultLore : ConfigText.parseList(loreLines));

        int stackSize = plugin.getConfig().getInt("cabezas-opciones.stack-maximo", 64);
        meta.setMaxStackSize(stackSize);
        if (plugin.getConfig().getBoolean("cabezas-opciones.brillo", true)) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());

        String textureUrl = plugin.getConfig().getString(basePath + ".textura", "");
        if (textureUrl != null && !textureUrl.isBlank()) {
            applyTexture(meta, type, textureUrl);
        }

        item.setItemMeta(meta);
        return item;
    }

    private void applyTexture(SkullMeta meta, GemType type, String textureUrl) {
        try {
            // UUID fijo por color, para que las cabezas se puedan apilar entre sí.
            UUID profileId = UUID.nameUUIDFromBytes(("offlnr-gem-" + type.name()).getBytes(StandardCharsets.UTF_8));
            PlayerProfile profile = Bukkit.createProfile(profileId, null);
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(textureUrl));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (MalformedURLException e) {
            plugin.getLogger().log(Level.WARNING, "URL de textura inválida en config.yml para una gema", e);
        }
    }

    public GemType readType(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) {
            return null;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        String raw = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        return raw == null ? null : GemType.valueOf(raw);
    }
}
