# Off-Gems

Plugin para servidores **Paper** que agrega un sistema de gemas de cristal
minables: bloques de colores que solo se pueden extraer con una herramienta
especial y que, al romperse, entregan una cabeza custom coleccionable
directo al inventario. Incluye un sistema de "minas" que se regeneran solas.

## Características

- 7 colores de gema (azul, morada, roja, verde, cian, amarilla, naranja),
  cada una es un bloque de vidrio teñido.
- **Azada de Gemas**: única herramienta capaz de minarlas — durabilidad
  infinita, brillo de encantamiento, y velocidad de picado configurable
  (por defecto 30s por gema), calculada con el sistema real de minado de
  Minecraft (sin trucos de paquetes ni animaciones falsas).
- Ningún otro ítem —ni la mano— puede ni rasguñar una gema.
- Al minarla, cae una cabeza custom (con textura, nombre y lore
  configurables) directo al inventario, sin tirarse al piso.
- **Minas**: llená una región entera de gemas de un color con un solo
  comando; cuando se agota la última, se regenera sola tras un tiempo
  configurable — ideal para minas de servidor tipo prision/survival.
- Nombre, lore y colores (por nombre, hex/rgb o gradientes) de la azada y
  de cada cabeza, 100% configurables vía `config.yml` con formato
  MiniMessage.

## Requisitos

- Servidor Paper 1.26.2 o superior.
- Java 26.

## Instalación

1. Compilar con Gradle (`./gradlew build`) o abrir el proyecto en IntelliJ
   (usa el plugin `run-paper`, que permite levantar un servidor de prueba
   con la tarea `runServer`).
2. Copiar el `.jar` generado en `build/libs/` a la carpeta `plugins/` del
   servidor.
3. Iniciar el servidor una vez para que se genere
   `plugins/OfflnrPlugin/config.yml`, y editarlo a gusto.

## Comandos

Todos bajo `/gema`, requieren el permiso `offlnr.gema` (por defecto: solo
operadores).

| Comando | Descripción |
|---|---|
| `/gema azada` | Entrega la Azada de Gemas. |
| `/gema cristal <color>` | Entrega un cristal colocable de ese color (se planta como un bloque normal y queda registrado como gema). |
| `/gema colocar <color>` | Convierte el bloque que estás mirando en una gema de ese color. |
| `/gema quitar` | Quita el registro de gema del bloque que estás mirando. |
| `/gema llenar <color> <x1> <y1> <z1> <x2> <y2> <z2>` | Llena esa región (cuboide) con gemas de ese color y la registra como una mina que se regenera sola cuando se agota. |
| `/gema minas` | Lista las minas creadas. |
| `/gema quitarmina <id>` | Deja de trackear una mina (no borra los bloques ya puestos en el mundo). |

Colores disponibles: `azul`, `morada`, `roja`, `verde`, `cian`, `amarilla`,
`naranja`.

## Configuración (`config.yml`)

- `azada.tiempo-minado-segundos`: segundos que tarda en romperse una gema
  (default `30`).
- `azada.nombre` / `azada.lore`: nombre y descripción de la Azada de Gemas
  (soporta MiniMessage: colores por nombre, hex `<#rrggbb>`, gradientes,
  negrita, subrayado, etc.).
- `minas.regeneracion-segundos`: segundos de espera antes de que una mina
  agotada se regenere entera (default `10`).
- `cabezas-opciones.stack-maximo` / `cabezas-opciones.brillo`: tamaño de
  stack y si las cabezas dropeadas brillan.
- `cabezas.<color>.textura` / `.nombre` / `.lore`: textura (URL de
  Mojang), nombre y descripción de la cabeza que dropea cada color.
