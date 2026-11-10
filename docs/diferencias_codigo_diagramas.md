# Diferencias entre el código y los diagramas originales

PPAI DSI 2026 · 3K5 · Grupo 06 · Actualizado al 10/11/2026

Los diagramas originales de `docs/diagramas/` (**DIAGRAMA CLASES**, **SECUENCIA PATRON** y
**MaquinaEstadosPPAI**) **no se modificaron**. Este archivo reúne las diferencias entre el código y los diagramas. Algunas aparecen al
implementar los flujos alternativos; otras necesitan validación con la tutora. No se presentan
como coincidencias exactas.
El detalle de cada flujo está en `docs/sesiones/Sesion_01` a `Sesion_05`.

## Qué diferencias ya existían y cuáles se agregaron

Antes de completar los alternativos, el grupo ya tenía el flujo normal, la estructura
Pantalla–Gestor–entidades y el patrón State. En esa primera implementación ya existían
diferencias como quién llama a `getNombre()`, el retorno de `Bolsin.buscarRemitos()` y
los parámetros de `Bolsin.crearCambioEstado()`. Estas no se originaron al desarrollar A3–A5.

Al completar A3, A4, A5 y la información del CU 29 se agregaron nuevas selecciones,
mensajes, atributos y parámetros; por ejemplo, `Bolsin.actualizarEstado()` pasó a
recibir los arreglos de documentación marcada. La implementación se amplió, pero los
diagramas definitivos no se modificaron.

El `AGENTS.md` inicial pedía actualizar primero el diagrama si cambiaba el código
(regla 1) y declarar todas las operaciones dependientes del estado (regla 7).
Por eso **dejar registradas estas diferencias no equivale a resolverlas**. Se deben
acordar con el grupo y, cuando corresponda, consultar a la tutora.

Categorías:
- **A. Necesarias** para implementar A3, A4, A5 y el contenido del CU 29. El diagrama de
  secuencia sólo modela el flujo descripto.
- **B. Justificadas** en el flujo descripto. Son decisiones de implementación con un motivo
  técnico.
- **C. Erratas del diagrama.** El código sigue a la secuencia.
- **D. No implementado.** Pendiente de decisión.

## A. Necesarias por los flujos alternativos y el CU 29

### A.1 Mensajes nuevos (no están en la secuencia)

| Emisor → receptor | Mensaje | Flujo |
|---|---|---|
| Gestor → Pantalla | `solicitarSeleccionDocumentacionNoRecibida()` | A3 |
| Gestor → Pantalla | `solicitarSeleccionDocumentacionRechazada()` | A4 |
| Gestor → Pantalla | `solicitarSeleccionDocumentacionParaRedirigir()` | A5 |
| EB → Pantalla → Gestor | `tomarSeleccionDocumentacion(nrosDocumentacion: int[])` | A3, A4, A5 |
| Gestor → Pantalla | `mostrarRegistroCancelado()` | A6 |
| Gestor → selec:Bolsin → ComisionMedica | `esDestinoCentral(): boolean` → `esCentral(): boolean` | A5 |
| Gestor → EstadoRemito | `*esRecibidoYAceptadoParcial(): boolean` (en el loop de estados de remito) | A3, A4 |
| Remito → DetalleRemito | `*esNoAceptada(nrosNoRecibida, nrosRechazada): boolean` | A3, A4 |
| DetalleRemito → Documentacion | `estaEntre(nros: int[]): boolean` | A3, A4, A5 |
| DetalleRemito → Documentacion | `registrarNoRecibida(...)`, `recibirYRechazar(...)`, `redirigir(...)` | A3, A4, A5 (ya están en el State) |
| Gestor → selec:Bolsin | `buscarOrigenesDocumentacion(): ComisionMedica[]` | CU 29 |
| Gestor → selec:Bolsin → Remito → DetalleRemito | `buscarSituacionDocumentacion(...)` | CU 29 |
| Remito | `getOrigen(): ComisionMedica` | CU 29 |
| Documentacion | `getNombreEstado(): String` (devuelve `estadoActual.getNombre()`) | CU 29 |
| Gestor → InterfazNotificacionBolsin | `notificarRecepcionBolsin(NotificacionRecepcionBolsin)`, **una vez por CM de origen** | CU 29 |

### A.2 Parámetros agregados a mensajes que sí están en la secuencia

Son los cambios que más se notan en la parte rediseñada. En el flujo descripto los arreglos
llegan **vacíos** y el comportamiento es exactamente el del diagrama.

| Mensaje del diagrama | Firma en el código |
|---|---|
| `selec:Bolsin.actualizarEstado(fhActual, estadoBolsin, estadoRemito, responsable)` | `+ estadoRemitoParcial, nrosNoRecibida, nrosRechazada, nrosParaRedirigir` (8 parámetros) |
| `Bolsin.actualizarRemitos(fhActual, estadoRemito, responsable)` | Los mismos 4 parámetros nuevos |
| `Remito.actualizarEstado(estadoRemito, fhActual, responsable)` | `+ estadoRemitoParcial, nrosNoRecibida, nrosRechazada, nrosParaRedirigir` |
| `Remito.actualizarDocumentacion(fhActual, responsable)` | `+ nrosNoRecibida, nrosRechazada, nrosParaRedirigir` |
| `DetalleRemito.actualizarDocumentacion(fhActual, responsable)` | `+ nrosNoRecibida, nrosRechazada, nrosParaRedirigir`. Decide entre `registrarNoRecibida`, `recibirYRechazar`, `redirigir` y `recibirYAceptar` |
| `buscarDatosDocumentacion()` (Remito y DetalleRemito) | Las filas ahora incluyen `nroDocumentacion` y `areaCMCDestino` |

### A.3 Atributos nuevos del Gestor y de la Pantalla

- **Gestor:**
  - `nrosDocumentacionNoRecibida`, `nrosDocumentacionRechazada` y
    `nrosDocumentacionParaRedirigir`: `int[]`;
  - `estadoRecibidoYAceptadoParcial`: `EstadoRemito`.
- **Pantalla:** `seleccionarDocumentacion: boolean`, que le indica al front si sigue el paso de
  marcar documentación.

### A.4 Clases que no están en el diagrama

- `InterfazNotificacionBolsin` y `NotificacionBolsinLog` ya estaban en la implementación inicial
  del grupo. Después se agregó `NotificacionRecepcionBolsin` para incluir la situación de cada
  documentación en los avisos del CU 29.
- `ManejadorErrores`: traduce las excepciones a 400 y 409.
- `EstadoDocumentacionConverter`: persistencia del estado por nombre.
- Los records de `dto` y los repositorios: diseño de persistencia y de la interfaz.

## B. Diferencias de implementación en el flujo descripto

| Diagrama | Código | Motivo |
|---|---|---|
| `getNombre()` lo envían Empleado y Bolsín a ComisionMedica, dentro de `obtenerCM()` y `getOrigen()` | El Gestor recibe la `ComisionMedica` y le pide `getNombre()` | La firma devuelve `ComisionMedica`, y el Gestor necesita el objeto (`esCmDestino`) y además el nombre |
| `Bolsin.buscarRemitos(): Remito[]` | Devuelve `int[]` | Se respeta `*getNumero()` de Bolsín a Remito y el gestor no se acopla a Remito |
| `Bolsin.crearCambioEstado()` sin parámetros | `crearCambioEstado(fhInicio, e, responsable)`, privado | El `new(fhInicio, e, usuarioResponsable)` necesita esos datos |
| `setFechaHoraFin` directo a Actual:CambioEstadoBolsin | Primero busca el actual con `sosActual()` | Es la misma búsqueda que la secuencia dibuja antes |
| `getOpcionesRecepcion(): void` (clases) | Devuelve `String[]` | Se pasa a `mostrarOpcionesRecepcionParaSeleccion` |
| `mostrarEjecucionExitosa()` antes del «include» | El CU 29 va antes del mensaje de éxito | No mostrar un éxito si falla la notificación |
| — | `saveAndFlush()`, `findAll()` y las validaciones del orden de los pasos | Diseño de persistencia y robustez. `saveAndFlush` escribe en la base antes de notificar |
| `Bolsin.buscarBolsinesEnviados(cmUsuario)` (clases) | Sin parámetro; la CM se pregunta con `esCmDestino` | Se sigue a la secuencia |
| Atributos del Gestor `nroPrecintoCM`, `origenCM`, `nroRemito`, `asuntoDocumentación`, `tipoDocumentación` | `nrosPrecinto[]`, `origenes[]`, `nrosRemito[]`, `documentacion[][]` | El gestor recuerda listas, no un solo valor |

## C. Diferencias de nombres y firmas entre diagramas

- `tomarConfirmaionRegistro` (le falta una "c").
- `FinCU()` en la secuencia y `finCU()` en clases.
- `esCmDestino` (secuencia) y `esCMDestino` (clases).
- `recibiryAceptar` y `recibiryRechazar` (clases) frente a `recibirYAceptar` (secuencia).
- `mostrarNumRemitosYDocumentaciónAsociada` (clases, con tilde y mayúscula).
- `esRecibidoyAceptado()` y `esRecibidoyAceptada()` figuran en **EstadoBolsin**; corresponden a
  **EstadoRemito**, que es a quien se lo pregunta la secuencia.
- `DetallesRemito` (clases) frente a `:DetalleRemito` (secuencia). El código usa `DetalleRemito`.
- `Gestor.actualizarRemitoyDocumentación()` está en clases, pero no en la secuencia ni en el código.
- `EnBolsinEnviado.recibirYAceptar(...)` figura con retorno `RecibidaYAceptada`; en el abstracto
  es `void`.

## D. No implementado, pendiente de decisión

- **Operaciones del State que pertenecen a otros CU.** El diagrama de clases las declara en
  `EstadoDocumentacion`, en `Documentacion` y en varios estados concretos, y el código no:
  `darDeBaja`, `enviar`, `incluirEnBolsin`, `incluirEnRemito`, `modificarBolsin`,
  `modificarRemito`, `removerDeBolsin`, `removerDeRemito` y `revisarDocumentacion` (más
  `cancelarRemito` en Documentación).
  - El código sólo declara las 4 transiciones del CU 28.
  - AGENTS.md regla 7 dice "declara todos los métodos dependientes del estado".
  - Ver `docs/decisiones_para_la_tutora.md`, punto 10.
- La máquina de estados no cambia. Las transiciones del CU 28 están todas implementadas, incluida
  `RecibidaYRechazada → ParaRedirigir` (sesión de la Fase 1), aunque ningún flujo del CU 28 la usa
  hoy.
