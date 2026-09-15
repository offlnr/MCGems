package org.offlnr.offlnrPlugin.gem;

import java.util.List;

/** Grupo de bloques de gema que se regeneran juntos. */
public record GemMine(String id, GemType type, List<GemLocation> locations) {
}
