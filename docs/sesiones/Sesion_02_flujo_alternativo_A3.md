# Sesión 02: flujo alternativo A3 "No se recibe toda la documentación"

PPAI DSI 2026 · 3K5 · Grupo 06 · Fecha: 10/11/2026

> **Nota (sesión 03):** para reutilizar el paso de selección en A4, algunos nombres de este
> documento se generalizaron. `tomarSeleccionDocumentacionNoRecibida` ahora es
> `tomarSeleccionDocumentacion` y `esNoRecibida` ahora es `esMarcada`. La lista completa está en
> `Sesion_03_flujo_alternativo_A4.md`.

Este documento describe la **extensión** que se agregó al diseño para implementar A3. Los
diagramas originales (`docs/diagramas/`) **no se modificaron**: el flujo descripto sigue siendo el
de `SECUENCIA PATRON.pdf`, y lo nuevo se presenta como extensión del flujo alternativo.

## 1. Regla de negocio implementada (ERS v1.2, "Recepción de un Bolsín en una CM Destino")

> No se recibe toda la documentación asociada a los remitos que contiene el bolsín: la
> documentación que no llegó queda como **no recibida** [...] el remito queda como **recibido y
> aceptado parcial**.

| Elemento | Resultado en A3 |
|---|---|
| Documentación marcada por el EB | EnBolsinEnviado → **NoRecibida** (`registrarNoRecibida`) |
| Resto de la documentación | EnBolsinEnviado → **RecibidaYAceptada** (`recibirYAceptar`) |
| Remito con al menos una documentación no recibida | **RecibidoYAceptadoParcial** |
| Remito completo | **RecibidoYAceptado** |
| Bolsín | **RecibidoEnCMDestino** (igual que en el flujo descripto) |
| Cambios de estado | Se cierra el actual (`setFechaHoraFin`) y se crea el nuevo con fecha y hora y el EB responsable |

No se implementan los CU que siguen después (volver a Registrada o dar de baja con el CU 31 o el
CU 9).

## 2. Mensajes nuevos (extensión de la secuencia)

Después de `tomarSeleccionOpcionRecepcion(opcion)`, si la opción es la 2:

```
Gestor  -> Pantalla : solicitarSeleccionDocumentacionNoRecibida()               [nuevo]
EB      -> Pantalla : tomarSeleccionDocumentacionNoRecibida(nrosDocumentacion: int[])   [nuevo]
Pantalla-> Gestor   : tomarSeleccionDocumentacionNoRecibida(nrosDocumentacion: int[])   [nuevo]
Gestor  -> Pantalla : solicitarConfirmacionRegistro()                            [ya existía]
EB      -> Pantalla : tomarConfirmacionRegistro(confirmacion)                    [ya existía]
...
Gestor  -> EstadoRemito : *esRecibidoYAceptadoParcial(): boolean                 [nuevo, en el loop de estados de remito]
Gestor  -> selec:Bolsin : actualizarEstado(fhActual, estadoBolsin, estadoRemito, responsable,
                                           estadoRemitoParcial, nrosNoRecibida)  [2 parámetros nuevos al final]
Bolsin  -> Remito       : actualizarEstado(estadoRemito, fhActual, responsable,
                                           estadoRemitoParcial, nrosNoRecibida)  [2 parámetros nuevos al final]
Remito  -> DetalleRemito: *esNoRecibida(nrosNoRecibida): boolean                 [nuevo: decide parcial / aceptado]
Remito  -> Remito       : setEstado(parcial o aceptado)
Remito  -> DetalleRemito: actualizarDocumentacion(fhActual, responsable, nrosNoRecibida)  [1 parámetro nuevo]
DetalleRemito -> Documentacion: estaEntre(nrosNoRecibida): boolean               [nuevo]
DetalleRemito -> Documentacion: registrarNoRecibida(fhActual, responsable)       (marcada)
                               | recibirYAceptar(fhActual, responsable)          (no marcada)
Documentacion -> EnBolsinEnviado: registrarNoRecibida(this, historial, fhActual, responsable)
EnBolsinEnviado -> new NoRecibida + cambiarEstado(...)                           (ya estaba en el State)
```

En el flujo descripto (opción 1), `nrosNoRecibida` es un arreglo **vacío**: ninguna documentación
está marcada, todos los remitos quedan RecibidoYAceptado y toda la documentación pasa por
`recibirYAceptar`. O sea, el flujo del diagrama es el caso particular de "nada marcado".

## 3. Diferencias con los diagramas

| Diagrama | Modelo original | Código después de A3 | Motivo |
|---|---|---|---|
| Clases: Documentación | `getAsunto()`, `getNombreTipo()` | + `getNumero(): int`, + `estaEntre(nros: int[]): boolean` | Identificar sin ambigüedad lo que marca el EB (el asunto se puede repetir) |
| Clases: DetallesRemito | `buscarDatosDocumentacion(): String[]` = [asunto, tipo] | Devuelve [nroDocumentacion, asunto, tipo]; + `esNoRecibida(nros): boolean`; `actualizarDocumentacion(fhActual, responsable, nrosNoRecibida)` | Experto: el detalle conoce su documentación y decide qué mensaje enviarle |
| Clases: Remito | `actualizarEstado(estadoRemito, fhActual, responsable)` | + `estadoRemitoParcial`, `nrosNoRecibida` al final | Experto: el remito conoce sus detalles y decide si queda parcial |
| Clases: Bolsín | `actualizarEstado(fhActual, estadoBolsin, estadoRemito, responsable)` | + `estadoRemitoParcial`, `nrosNoRecibida` al final | Les pasa los datos a sus remitos |
| Clases: EstadoRemito | `esRecibidoyAceptado()` | + `esRecibidoYAceptadoParcial(): boolean` | Puntero al estado parcial, igual que el loop existente |
| Clases: Gestor | atributos del diagrama | + `nrosDocumentacionNoRecibida: int[]`, + `estadoRecibidoYAceptadoParcial: EstadoRemito`, + `tomarSeleccionDocumentacionNoRecibida(nros: int[])` | Recuerda la selección del actor (Controlador) |
| Clases: Pantalla | — | + `solicitarSeleccionDocumentacionNoRecibida()`, + `tomarSeleccionDocumentacionNoRecibida(nros: int[])` | Diálogo nuevo con el EB |
| Secuencia | Sólo opción 1 | Mensajes de la sección 2 | El diagrama no modela A3 |
| Máquina de estados | EnBolsinEnviado → NoRecibida `[no se recibió]/registrarNoRecibida()` | Sin cambios: ya estaba en el State | — |

El **patrón State no cambió**: `EnBolsinEnviado.registrarNoRecibida` ya existía y tenía prueba. A3
sólo agrega quién decide enviar ese mensaje (DetalleRemito).

## 4. Decisiones de diseño

1. **Identificación.** Se usa `Documentacion.numero`, que es la clave primaria. El asunto no
   sirve: dos documentaciones pueden tener el mismo asunto.
2. **Responsabilidades (GRASP).**
   - El Gestor sólo recuerda la opción y la documentación marcada, y las pasa.
   - `DetalleRemito` decide qué transición pedirle a su documentación.
   - `Remito` decide si queda parcial.
   - El **estado actual** decide si la transición es válida.
   - Ninguna clase pregunta el nombre de un estado de documentación.
3. **Confirmación.** El orden es opción 2 → marcar documentación → confirmar.
   - El gestor no deja confirmar sin marcar (409).
   - Con la opción 1 no deja marcar (409).
   - Rechaza una selección vacía o con documentación de otro bolsín (400).
   - Si el EB no confirma (A6), no se modifica nada.
4. **Datos de prueba.** No hizo falta cambiar `data.sql`: el estado `RecibidoYAceptadoParcial` ya
   existía.
5. **No se valida que quede al menos una documentación recibida.** La ERS no lo pide, así que no
   se inventó esa regla.
6. **CU 29 sin cambios.** La notificación sigue igual. Informar "la situación de la documentación"
   queda para cuando se trabaje el CU 29.
7. **Opción elegida.** La única comparación con el texto de la opción está en el gestor: decide si
   hay que pedir la marca. No hay ningún `if` sobre estados.

## 5. Pruebas

| Prueba | Tipo | Qué verifica |
|---|---|---|
| `EstadoDocumentacionTest` (9) | Unitaria (State) | Transiciones, rechazo por defecto, historial y convertidor |
| `SeleccionBolsinResponseTest` | Unitaria | La pantalla agrupa por remito y conserva el número de cada documentación aunque se repita el asunto |
| `RegistroRecepcionBolsinIntegracionTest` (4) | Integración (Spring + H2 en memoria) | Flujo descripto (5002); A3 marcando la documentación 2 del 5001 (remito 1001 parcial, 1002 aceptado, historial y responsable); A6 después de marcar (5005 sin cambios); validaciones del orden y de la selección |

Prueba manual en el front (10/11):
- A3 con el 5001, marcando "Dictamen incapacidad - Gómez, Ana";
- flujo descripto con el 5002;
- A6 con el 5005, después de marcar y de usar "Elegir otra opción".

Después se revisó la base en `/h2-console`.

## 6. Pendiente

- A4 y A5.
- Actualizar `PENDIENTE.txt`, `COMO_EJECUTAR.txt` y `CONTEXTO_IA.md`: todavía dicen que las
  opciones 2 a 4 no están implementadas.
- Decidir con la tutora si la extensión se dibuja en el EA (fragmento `alt`).
