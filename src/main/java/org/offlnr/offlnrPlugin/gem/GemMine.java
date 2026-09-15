package org.offlnr.offlnrPlugin.gem;

import java.util.List;

/**
 * Un grupo de bloques de gema creado de una sola vez (ej: con
 * {@code /gema llenar}), todos del mismo color, que se regeneran juntos
 * cuando se agota el último.
 */
public record GemMine(String id, GemType type, List<GemLocation> locations) {
}
