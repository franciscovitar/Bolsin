# Sesión 03: flujo alternativo A4 "Existe documentación que no corresponde al destino"

PPAI DSI 2026 · 3K5 · Grupo 06 · Fecha: 10/11/2026

Este documento describe la **extensión** que se agregó al diseño para implementar A4, reutilizando
lo que se hizo para A3 (sesión 02). Los diagramas originales (`docs/diagramas/`) **no se
modificaron**.

> **Cambio de nombres respecto de la sesión 02.** Para que A3 y A4 usen el mismo paso de
> selección, se generalizaron algunos nombres:
> - `tomarSeleccionDocumentacionNoRecibida(nros)` pasó a llamarse `tomarSeleccionDocumentacion(nros)`,
>   tanto en la Pantalla como en el Gestor, y el endpoint también.
> - `DetalleRemito.esNoRecibida(nros)` pasó a llamarse `esMarcada(nrosNoRecibida, nrosRechazada)`.
> - El campo `seleccionarDocumentacionNoRecibida` de la respuesta pasó a llamarse
>   `seleccionarDocumentacion`.
> - `DocumentacionNoRecibida.jsx` pasó a llamarse `SeleccionDocumentacion.jsx`.
>
> `solicitarSeleccionDocumentacionNoRecibida()` sigue igual.

## 1. Regla de negocio (ERS v1.2, "Recepción de un Bolsín en una CM Destino")

> Documentación que no corresponde a la CM destino: la documentación se marca como **recibida y
> rechazada**. Esta documentación debe ser enviada de vuelta al origen [...].

| Elemento | Resultado en A4 | Fuente |
|---|---|---|
| Documentación marcada por el EB | EnBolsinEnviado → **RecibidaYRechazada** (`recibirYRechazar`) | ERS + máquina de estados (`[la documentacion no corresponde a la cm destino]/recibiryRechazar()`) |
| Resto de la documentación | EnBolsinEnviado → **RecibidaYAceptada** (`recibirYAceptar`) | ERS |
| Bolsín | **RecibidoEnCMDestino** | ERS ("Al momento de la recepción el bolsín se registra como recibido en CM destino") |
| Remito con documentación rechazada | **RecibidoYAceptadoParcial** | **Decisión provisional del grupo**, que hay que validar con la tutora. La ERS **no** lo establece |
| Remito sin documentación rechazada | **RecibidoYAceptado** | Igual que en el flujo descripto |
| CU 29 | Se invoca igual que en el flujo descripto | Paso 12 del CU |

El envío de vuelta al origen y la revisión posterior (CU 31) son de otros casos de uso y no se
implementan.

### Por qué RecibidoYAceptadoParcial es coherente (decisión provisional)

- Para un remito, en la recepción la ERS sólo nombra dos estados: "recibido y aceptado" (todo
  aceptado) y "recibido y aceptado parcial" (A3). `data.sql` no tiene ningún estado de remito
  "rechazado", y la ERS tampoco lo menciona.
- En A4, una parte del remito se acepta y otra no, que es lo mismo que describe "aceptado
  parcial". Usar RecibidoYAceptado ocultaría que hubo documentación rechazada.
- No se encontró ninguna regla que lo contradiga.
- **Caso límite a consultar:** un remito con **toda** su documentación rechazada (o, en A3, toda no
  recibida) también queda "aceptado parcial", aunque no se haya aceptado nada.

## 2. Mensajes nuevos o generalizados (extensión de la secuencia)

```
EB      -> Pantalla : tomarSeleccionOpcionRecepcion("Existe documentación que no corresponde al destino")
Pantalla-> Gestor   : tomarSeleccionOpcionRecepcion(opcion)
Gestor  -> Pantalla : solicitarSeleccionDocumentacionRechazada()                  [nuevo, A4]
EB      -> Pantalla : tomarSeleccionDocumentacion(nrosDocumentacion: int[])       [generalizado A3/A4]
Pantalla-> Gestor   : tomarSeleccionDocumentacion(nrosDocumentacion: int[])       [generalizado A3/A4]
Gestor  -> Pantalla : solicitarConfirmacionRegistro()
EB      -> Pantalla : tomarConfirmacionRegistro(confirmacion)
Gestor  -> selec:Bolsin : actualizarEstado(fhActual, estadoBolsin, estadoRemito, responsable,
                                           estadoRemitoParcial, nrosNoRecibida, nrosRechazada)   [+1 parámetro]
Bolsin  -> Remito       : actualizarEstado(estadoRemito, fhActual, responsable,
                                           estadoRemitoParcial, nrosNoRecibida, nrosRechazada)   [+1 parámetro]
Remito  -> DetalleRemito: *esMarcada(nrosNoRecibida, nrosRechazada): boolean       [generaliza esNoRecibida]
Remito  -> DetalleRemito: actualizarDocumentacion(fhActual, responsable, nrosNoRecibida, nrosRechazada)
DetalleRemito -> Documentacion: estaEntre(nros): boolean
DetalleRemito -> Documentacion: registrarNoRecibida(...)   (A3: marcada como no recibida)
                              | recibirYRechazar(...)      (A4: marcada como que no corresponde)
                              | recibirYAceptar(...)       (no marcada)
Documentacion -> EnBolsinEnviado: recibirYRechazar(this, historial, fhActual, responsable)
EnBolsinEnviado -> new RecibidaYRechazada + cambiarEstado(...)                     (ya estaba en el State)
Gestor  -> CU 29 «include»                                                         (sin cambios)
```

En el flujo descripto los dos arreglos llegan vacíos, así que el comportamiento es igual al del
diagrama. En A3 sólo se completa `nrosNoRecibida` y en A4 sólo `nrosRechazada`.

## 3. Diferencias con los diagramas (acumuladas A3 + A4)

| Diagrama | Modelo original | Código actual |
|---|---|---|
| Clases: Pantalla | — | + `solicitarSeleccionDocumentacionNoRecibida()`, + `solicitarSeleccionDocumentacionRechazada()`, + `tomarSeleccionDocumentacion(nros: int[])` |
| Clases: Gestor | atributos del diagrama | + `nrosDocumentacionNoRecibida: int[]`, + `nrosDocumentacionRechazada: int[]`, + `estadoRecibidoYAceptadoParcial: EstadoRemito`, + `tomarSeleccionDocumentacion(nros: int[])` |
| Clases: Bolsín | `actualizarEstado(fhActual, estadoBolsin, estadoRemito, responsable)` | + `estadoRemitoParcial, nrosNoRecibida, nrosRechazada` al final |
| Clases: Remito | `actualizarEstado(estadoRemito, fhActual, responsable)` | + `estadoRemitoParcial, nrosNoRecibida, nrosRechazada` al final |
| Clases: DetallesRemito | `actualizarDocumentacion(fhActual, responsable)`, `buscarDatosDocumentacion(): String[]` | + `nrosNoRecibida, nrosRechazada`; + `esMarcada(...)`; devuelve también el número de documentación |
| Clases: Documentación | — | + `getNumero(): int`, + `estaEntre(nros: int[]): boolean` |
| Clases: EstadoRemito | `esRecibidoyAceptado()` | + `esRecibidoYAceptadoParcial(): boolean` |
| Secuencia | Sólo la opción 1 | Mensajes de la sección 2 |
| Máquina de estados / State | EnBolsinEnviado → RecibidaYRechazada | **Sin cambios**: `EnBolsinEnviado.recibirYRechazar` ya existía y tenía prueba |

## 4. Decisiones de diseño

1. **Reutilización de A3.** Hay un solo mensaje de selección (`tomarSeleccionDocumentacion`), una
   sola validación, un solo componente de front (`SeleccionDocumentacion.jsx`) y un solo criterio de
   remito parcial (`Remito.tieneDocumentacionMarcada`). Lo único que cambia entre A3 y A4 es:
   - el texto que muestra la pantalla para pedir la selección;
   - en qué arreglo guarda el gestor la selección;
   - qué mensaje le envía `DetalleRemito` a la documentación marcada.
2. **Dos arreglos en lugar de un objeto "opción".** El modelo recibe `nrosNoRecibida` y
   `nrosRechazada`. Así, cada entidad decide con datos que entiende (Experto), y no hace falta una
   clase nueva que no figura en el modelo de dominio. La interpretación de la opción elegida queda
   en un solo lugar del gestor (Controlador). Modelar cada opción como un objeto polimórfico queda
   como alternativa, para consultar con la tutora si se quiere eliminar esa comparación de textos.
3. **State.** Ninguna clase pregunta el nombre de un estado de documentación. La transición la
   resuelve el estado concreto: `EnBolsinEnviado.recibirYRechazar` crea `RecibidaYRechazada` y usa
   `cambiarEstado` heredado, que cierra el cambio de estado actual, crea el nuevo con fecha, hora y
   responsable, y se lo asigna.
4. **Validaciones sin modificaciones parciales:**
   - Se valida **toda** la selección antes de guardarla. Si un número no pertenece al bolsín, no se
     guarda ninguno (por ejemplo, `[8, 1]` en el 5005 no deja marcada la 8).
   - Una selección vacía da 400.
   - Confirmar sin haber marcado da 409.
   - Marcar con la opción 1 da 409.
   - Si el EB cambia de opción, se descarta la marca anterior.
5. **Confirmación obligatoria.** Nada se persiste hasta `tomarConfirmacionRegistro(true)`.
   `actualizarBolsin()` corre dentro de una transacción. Si falla algo al actualizar o al invocar el
   CU 29, la transacción se revierte y el gestor descarta el registro en curso (`finCU`), para no
   reutilizar objetos a medio modificar.
6. **CU 29** se invoca igual que en el flujo descripto. Informar la situación de la documentación
   rechazada sigue pendiente (ver sección 6).

## 5. Pruebas

| Prueba | Qué verifica |
|---|---|
| `a4LaDocumentacionMarcadaQuedaRechazadaYElRestoDelMismoRemitoAceptada` | 5001, marcando la 1:<br>• la 1 queda RecibidaYRechazada y la 2 RecibidaYAceptada, **en el mismo remito 1001**, y la 3 RecibidaYAceptada;<br>• el 1001 queda RecibidoYAceptadoParcial y el 1002 RecibidoYAceptado;<br>• el bolsín 101 queda RecibidoEnCMDestino;<br>• historial cerrado y abierto en el mismo instante que el bolsín, con responsable Laura;<br>• CU 29 invocado |
| `a4UnaSeleccionInvalidaNoGuardaNadaYCambiarDeOpcionDescartaLaMarca` | `[8, 1]` no guarda nada; selección vacía; confirmar sin marcar; al cambiar a la opción 1 se descarta la marca y todo queda aceptado |
| `a6DespuesDeMarcarNoConfirmaYNadaCambia` | A4 + no confirmar: el 5005 no cambia y **no** se invoca el CU 29 |
| `a3...` y `flujoDescripto...` | Regresión de A3 y del flujo descripto, que ahora también verifican el CU 29 |
| `EstadoDocumentacionTest` (9) y `SeleccionBolsinResponseTest` (1) | State y agrupación por remito |

`RegistroRecepcionBolsinIntegracionTest` usa una H2 en memoria y `@DirtiesContext`, así que cada
prueba empieza con la base recién cargada. El CU 29 se verifica con un `@MockitoSpyBean` sobre
`InterfazNotificacionBolsin`.

Prueba manual del 10/11:
- **En el front:**
  - A4 con el 5005, marcando "Historia clínica - Paz, Eva": la 8 quedó RecibidaYRechazada, la 9
    RecibidaYAceptada y el remito 1006, parcial;
  - A6 con el 5002, después de marcar con la opción 3.
- **Por la API, a través del proxy del front:**
  - A3 con el 5001, marcando la 3;
  - flujo descripto con el 5002.
- **En `/h2-console`:** se revisaron los estados y el historial.

## 6. Pendiente e inconsistencias

- **Validar con la tutora** el remito RecibidoYAceptadoParcial en A4 y el caso de un remito con
  toda su documentación marcada.
- **CU 29:** la ERS pide notificar "la situación de la documentación recibida" a cada CM de origen.
  Hoy sólo se avisa la recepción a la CM de origen del bolsín.
- **Comparación del texto de la opción en el gestor:** ver la decisión 2.
- **Documentación:** `PENDIENTE.txt`, `COMO_EJECUTAR.txt` y `CONTEXTO_IA.md` siguen diciendo que
  las opciones 2 a 4 no están implementadas.
- **A5:** sin implementar. Responde "todavía no está implementada".
