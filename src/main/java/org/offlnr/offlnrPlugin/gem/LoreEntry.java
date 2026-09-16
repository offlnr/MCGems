package org.offlnr.offlnrPlugin.gem;

import net.kyori.adventure.text.Component;

/**
 * Un encantamiento o atributo listo para formatear en el lore: nombre visible y nivel en romano.
 * El nombre es un Component (no un String plano) para poder usar componentes traducibles: los
 * encantamientos, por ejemplo, se muestran en el idioma configurado en el cliente de cada jugador.
 */
public record LoreEntry(Component name, String level) {
}
