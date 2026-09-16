package org.offlnr.offlnrPlugin.gem;

import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Función para leer la configuración del haz de luz de un ítem ({@code <path>.rayo.activo} y
 * {@code <path>.rayo.color} en items.yml). El color es uno de los colores de gema disponibles
 * ({@link GemType#id()}); si no está configurado o es inválido, se usa el color por defecto.
 */
public final class GemBeamConfig {

    private GemBeamConfig() {
    }

    public static boolean isActive(FileConfiguration config, String path, boolean defaultActive) {
        return config.getBoolean(path + ".rayo.activo", defaultActive);
    }

    public static Color resolveColor(FileConfiguration config, String path, GemType defaultColor) {
        GemType type = GemType.fromId(config.getString(path + ".rayo.color"));
        if (type == null) {
            type = defaultColor;
        }
        return Color.fromRGB(type.color().value());
    }
}
