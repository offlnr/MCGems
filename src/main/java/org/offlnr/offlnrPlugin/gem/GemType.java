package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.Locale;

/** Los seis colores de gema soportados. */
public enum GemType {

    CELESTE("celeste", "Gema Celeste", Material.LIGHT_BLUE_STAINED_GLASS, TextColor.color(0x55FFFF), "celeste",
            "Zero Gem of Revelation", "69980"),
    PURPLE("morada", "Gema Morada", Material.PURPLE_STAINED_GLASS, TextColor.color(0xAA00FF), "purple",
            "Zero Gem of Disdain", "69982"),
    RED("roja", "Gema Roja", Material.RED_STAINED_GLASS, TextColor.color(0xFF5555), "red",
            "Zero Gem of Hatred", "69976"),
    GREEN("verde", "Gema Verde", Material.LIME_STAINED_GLASS, TextColor.color(0xA6FF00), "green",
            "Zero Gem of Abhorrence", "69979"),
    YELLOW("amarilla", "Gema Amarilla", Material.YELLOW_STAINED_GLASS, TextColor.color(0xFFFF55), "yellow",
            "Zero Gem of Contenment", "69978"),
    ORANGE("naranja", "Gema Naranja", Material.ORANGE_STAINED_GLASS, TextColor.color(0xFFAA55), "orange",
            "Zero Gem of Dread", "69977");

    private final String id;
    private final String displayName;
    private final Material blockMaterial;
    private final TextColor color;
    private final String configKey;
    private final String headTitle;
    private final String headId;

    GemType(String id, String displayName, Material blockMaterial, TextColor color, String configKey,
            String headTitle, String headId) {
        this.id = id;
        this.displayName = displayName;
        this.blockMaterial = blockMaterial;
        this.color = color;
        this.configKey = configKey;
        this.headTitle = headTitle;
        this.headId = headId;
    }

    /** Identificador corto usado en comandos (ej: "celeste"). */
    public String id() {
        return id;
    }

    /** Nombre corto en español, usado en mensajes de chat. */
    public String displayName() {
        return displayName;
    }

    public Material blockMaterial() {
        return blockMaterial;
    }

    public TextColor color() {
        return color;
    }

    /** Clave usada en config.yml para la textura de cabeza de esta gema. */
    public String configKey() {
        return configKey;
    }

    /** Nombre temático de la cabeza (serie "Zero Gem" de minecraft-heads.com). */
    public String headTitle() {
        return headTitle;
    }

    /** ID de la cabeza en minecraft-heads.com, mostrado en el lore. */
    public String headId() {
        return headId;
    }

    public static GemType fromId(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (GemType type : values()) {
            if (type.id.equals(normalized) || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static GemType fromMaterial(Material material) {
        for (GemType type : values()) {
            if (type.blockMaterial == material) {
                return type;
            }
        }
        return null;
    }
}
