package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;
import java.util.stream.Collectors;

/** Función para convertir texto de config.yml/items.yml a Components con MiniMessage. */
public final class ConfigText {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ConfigText() {
    }

    /** Parsea el texto, o usa el fallback si está vacío. */
    public static Component parse(String raw, Component fallback, TagResolver... resolvers) {
        Component base = (raw == null || raw.isBlank()) ? fallback : MINI_MESSAGE.deserialize(raw, resolvers);
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
