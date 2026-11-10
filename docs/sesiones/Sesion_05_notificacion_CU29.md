# Sesión 05: notificación del CU 29 incluida desde el CU 28

PPAI DSI 2026 · 3K5 · Grupo 06 · Fecha: 10/11/2026

Se completó el «include» del CU 29: la notificación ahora informa la recepción del bolsín y la
situación de la documentación que mandó **cada** comisión médica de origen. Se sigue registrando en
el log (`NotificacionBolsinLog`); no se envían correos reales. Los diagramas originales no se
modificaron.

## 1. Qué pide la ERS

| Fuente | Texto |
|---|---|
| CU 28, paso 12 | "para enviar notificación a **cada CM de origen que envió documentación**, sobre la recepción del bolsín y la situación de la documentación recibida (leer Obs. 2), se incluye al caso de uso 29" |
| Lista de CU, CU 29 | "Notificar por email a la comisión médica origen la recepción del bolsín y la situación de la documentación **correspondiente a la misma**" |
| Obs. 2 del CU 28 | La situación se explica en la regla "Recepción de un Bolsín en una CM Destino": aceptada, no recibida, rechazada, para redirigir |
| RNF 5 | "Se debe notificar vía email **al momento de recibir un bolsín a la CM destino** [...] detallando toda la documentación enviada en cada Bolsín" |

**Inconsistencia de la ERS:** el RNF 5 habla de notificar a la **CM destino**, pero el CU 28 y el
CU 29 hablan de la **CM de origen**. Se siguió a los dos casos de uso, que son el alcance del
trabajo. El RNF 5 se cumple en lo técnico: hay un componente intercambiable
(`InterfazNotificacionBolsin`). Conviene consultarlo con la tutora.

## 2. Diseño

```
Gestor.llamarCasoUso29()
  for cmOrigen in selec:Bolsin.buscarOrigenesDocumentacion(): ComisionMedica[]           [nuevo]
      Bolsin -> Remito : *getOrigen(): ComisionMedica                                     [nuevo]
    selec:Bolsin.buscarSituacionDocumentacion(cmOrigen): String[][]                       [nuevo]
      Bolsin -> Remito : *getOrigen() (filtra los remitos de esa CM)
      Bolsin -> Remito : buscarSituacionDocumentacion(): String[][]                       [nuevo]
        Remito -> DetalleRemito : *buscarSituacionDocumentacion(): String[]               [nuevo]
          DetalleRemito -> Documentacion : getNumero(), getAsunto(), getNombreEstado()   [getNombreEstado nuevo]
            Documentacion -> EstadoDocumentacion : getNombre()                            (ya existía)
    Gestor -> InterfazNotificacionBolsin : notificarRecepcionBolsin(NotificacionRecepcionBolsin)   [firma nueva]
```

- **CM de origen de cada documentación.** Sale de `Remito.origen`, la CM que generó el remito, que
  existe en la entidad y en el diagrama de clases.
  - El modelo de dominio también le da a Documentación un `cmOrigen`, pero esa asociación no está
    en nuestro diagrama de clases de diseño ni en la entidad, así que no se agregó.
  - No se asume que todos los remitos de un bolsín tengan el mismo origen que el bolsín.
- **Una notificación por CM de origen.** Cada CM recibe sólo la documentación de sus remitos. Los
  remitos se agrupan por CM con `ComisionMedica.equals`, que compara por `codigo`.
- **Experto y Alta Cohesión:**
  - Bolsín sabe qué remitos tiene y de qué CM son;
  - Remito arma las filas de su documentación;
  - DetalleRemito y Documentación aportan número, asunto y nombre del estado.

  Los **estados concretos no tienen ninguna lógica de notificación**: sólo se lee el nombre que ya
  tenían.
- **Bajo acoplamiento:**
  - El modelo no conoce el paquete `notificacion`: devuelve filas `String[][]`, como en los demás
    mensajes del diagrama.
  - `NotificacionRecepcionBolsin` es un record del paquete `notificacion`. Arma, con
    `de(...)`, los datos de **un** destinatario: email y nombre de la CM de origen, precinto, CM
    destino y la lista de `SituacionDocumentacion(nroRemito, nroDocumentacion, asunto, estado)`.
  - `InterfazNotificacionBolsin` conserva su nombre y su papel; sólo cambió la firma.
  - `NotificacionBolsinLog.redactar(...)` arma el texto y lo deja en el log.
- **Transacción.** `llamarCasoUso29()` sigue corriendo dentro de la transacción de
  `tomarConfirmacionRegistro`, después de actualizar y guardar. Si la notificación falla:
  - la transacción se revierte, y no queda nada guardado;
  - el gestor descarta el registro en curso (`finCU`);
  - la pantalla **no** muestra el éxito.
- **Mensaje de éxito.** Ahora dice "se notificó a **cada** comisión médica de origen".

## 3. Diferencias con los diagramas

| Diagrama | Modelo | Código |
|---|---|---|
| Secuencia | `llamarCasoUso29()` y un `«include»` | Un `notificarRecepcionBolsin` por cada CM de origen, con los mensajes de la sección 2 |
| Clases: Bolsín | — | + `buscarOrigenesDocumentacion(): ComisionMedica[]`, + `buscarSituacionDocumentacion(cmOrigen): String[][]` |
| Clases: Remito | — | + `getOrigen(): ComisionMedica`, + `buscarSituacionDocumentacion(): String[][]` |
| Clases: DetallesRemito | — | + `buscarSituacionDocumentacion(): String[]` |
| Clases: Documentación | — | + `getNombreEstado(): String` |
| Clases (nuevas, del paquete de notificación) | — | `InterfazNotificacionBolsin`, `NotificacionBolsinLog`, `NotificacionRecepcionBolsin` |

## 4. Ejemplos reales (salida del log de las pruebas)

A4 con el 5001, marcando la documentación 1:
```
CU 29 - Correo a cm.central@bolsines.test (Comisión Médica Central): el bolsín con precinto 5001 fue recibido en Comisión Médica Córdoba. Situación de la documentación enviada:
  - Remito 1001, documentación 1 "Expediente 1/2026 - Pérez, Juan": RecibidaYRechazada
  - Remito 1001, documentación 2 "Dictamen incapacidad - Gómez, Ana": RecibidaYAceptada
  - Remito 1002, documentación 3 "Historia clínica - Ríos, Pedro": RecibidaYAceptada
```

Varias CM de origen: fixture `datos-varios-origenes.sql`, A4 con el 5001, marcando la 13. Se
generan dos correos y cada uno tiene sólo su documentación:
```
CU 29 - Correo a cm.central@bolsines.test (Comisión Médica Central): el bolsín con precinto 5001 fue recibido en Comisión Médica Córdoba. Situación de la documentación enviada:
  - Remito 1001, documentación 1 "Expediente 1/2026 - Pérez, Juan": RecibidaYAceptada
  - Remito 1001, documentación 2 "Dictamen incapacidad - Gómez, Ana": RecibidaYAceptada
  - Remito 1002, documentación 3 "Historia clínica - Ríos, Pedro": RecibidaYAceptada
CU 29 - Correo a cm.rosario@bolsines.test (Comisión Médica Rosario): el bolsín con precinto 5001 fue recibido en Comisión Médica Córdoba. Situación de la documentación enviada:
  - Remito 1009, documentación 13 "Dictamen - Ferrer, Inés": RecibidaYRechazada
```

## 5. Pruebas

| Prueba | Qué verifica |
|---|---|
| `NotificacionBolsinLogTest` (unitaria) | El texto **exacto** del correo |
| Flujo descripto (5002) | Una notificación a Rosario con las documentaciones 4 y 5 en RecibidaYAceptada |
| A3 (5001, marca la 2) | Central: 1 y 3 RecibidaYAceptada, 2 NoRecibida |
| A4 (5001, marca la 1) | Central: 1 RecibidaYRechazada, 2 y 3 RecibidaYAceptada |
| A5 (fixture CMC, 5006, marca la 10) | Córdoba: 10 ParaRedirigir, 11 y 12 RecibidaYAceptada; CM destino: Central |
| `cu29ConVariasCMOrigenCadaUnaRecibeSoloSuDocumentacion` | Dos notificaciones, Central (1, 2 y 3) y Rosario (sólo la 13), sin mezclar documentación |
| A6 (con la opción 3 y con la 4) | Ninguna notificación |
| `siFallaLaNotificacionNoSeRegistraLaRecepcion` | Si la notificación falla:<br>• nada queda persistido (estados e historial iguales);<br>• no se muestra el éxito;<br>• no se puede reintentar sobre objetos a medio actualizar |

Las pruebas de integración capturan la `NotificacionRecepcionBolsin` real con un `ArgumentCaptor`
sobre el `@MockitoSpyBean` y comparan todo su contenido: email, nombre de la CM de origen, precinto,
CM destino y cada documentación con su estado.

Resultado con JDK 21 (`mvnw -o clean install`): **23 pruebas, 0 fallas**.

En el front se probó A3 con el 5001, marcando la 3. La pantalla muestra "Se registró la recepción
del bolsín y se notificó a cada comisión médica de origen.", y el log del back tiene el correo con
la 3 en NoRecibida.

## 6. Pendiente

- Consultar la inconsistencia entre el RNF 5 (CM destino) y el CU 28/29 (CM de origen).
- El estado se informa con el nombre técnico (por ejemplo, `RecibidaYAceptada`). Mostrar un texto
  más amable exigiría una descripción por estado. Se podría agregar en el State, sin lógica de
  notificación.
- Al integrar un servidor de correo real, sólo cambia la implementación de
  `InterfazNotificacionBolsin`.
