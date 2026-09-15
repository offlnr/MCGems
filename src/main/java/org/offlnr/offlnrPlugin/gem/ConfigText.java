package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte texto configurado en config.yml a Components de Adventure usando
 * MiniMessage — ya viene incluido con Paper, no hace falta ningún plugin
 * extra. Soporta colores por nombre ({@code <red>}, {@code <gold>}...),
 * colores hex/rgb ({@code <#ff8800>} o {@code <color:#ff8800>}) y estilos
 * ({@code <bold>}, {@code <italic>}, {@code <underlined>}, etc.).
 */
public final class ConfigText {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ConfigText() {
    }

    /** Parsea {@code raw}, o usa {@code fallback} si está vacío/ausente. En ambos casos deja el ítalic en false salvo que el texto lo pida explícitamente. */
    public static Component parse(String raw, Component fallback) {
        Component base = (raw == null || raw.isBlank()) ? fallback : MINI_MESSAGE.deserialize(raw);
        return base.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static List<Component> parseList(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        return raw.stream()
                .map(line -> parse(line, Component.empty()))
                .collect(Collectors.toList());
    }
}
