# Decisiones para revisar con la tutora

PPAI DSI 2026 · 3K5 · Grupo 06 · Actualizado al 10/11/2026

Estos puntos requieren una decisión del grupo o una consulta con la tutora. En algunos casos,
la ERS no define qué hacer; en otros, distintas indicaciones no coinciden. La tabla aclara qué
se hizo y qué alternativas existen.

Además de las decisiones de la tabla, hay dos diferencias con las **reglas originales del
grupo** que necesitan acuerdo:

- La regla 1 de `AGENTS.md` indicaba corregir primero el diagrama cuando cambiaba el código.
  Durante la implementación posterior se conservaron los diagramas, pero se añadieron
  mensajes y parámetros para A3, A4, A5 y CU 29. La documentación de esas diferencias
  no significa que ya se haya aprobado esa forma de presentarlas (**punto 11**).
- La regla 7 pedía que `EstadoDocumentacion` declarara todas las operaciones dependientes
  del estado. La primera versión del grupo ya implementaba solamente las cuatro del CU 28.
  Esto sigue pendiente de decisión (**punto 10**).

Los estados de remitos en A4/A5, la validación del área y el reconocimiento de la Central
fueron decisiones tomadas al completar los alternativos. No se deben atribuir como
definiciones previas del grupo ni como requisitos explícitos de la ERS.

| # | Tema | Qué dice la ERS | Qué se implementó | Alternativa |
|---|---|---|---|---|
| 1 | Estado del remito en **A4** | No lo dice | **RecibidoYAceptadoParcial** si tiene documentación rechazada | RecibidoYAceptado |
| 2 | Estado del remito en **A5** | No lo dice | **RecibidoYAceptado**: la documentación redirigida corresponde a la CMC y llegó; la redirección es interna | RecibidoYAceptadoParcial |
| 3 | Remito con **toda** su documentación marcada (A3 o A4) | No lo dice | También queda "aceptado parcial", aunque no se haya aceptado nada | Agregar un estado o exigir que quede al menos una aceptada |
| 4 | **A5** con documentación sin `areaCMCDestino` | No lo dice. La ERS habla de "un área dentro de la sede central" | Se rechaza (400): sólo se redirige la documentación con un área registrada en su detalle de remito | Permitirlo sin área, o que el EB indique el área (sería un dato nuevo) |
| 5 | Cómo se reconoce la **CMC** | Glosario: "CMC: Comisión médica central" (una sola). El modelo de dominio no tiene un atributo para eso | `ComisionMedica.esCentral()` compara el nombre "Comisión Médica Central", con el mismo criterio que `EstadoBolsin.sosEnviado()` | Un atributo booleano o un código fijo, que agregaría información fuera del modelo de dominio |
| 6 | **Destinatario de la notificación** | CU 28 paso 12 y CU 29: **CM de origen**. RNF 5: "a la **CM destino**" | Se sigue a los CU: una notificación por cada CM de origen, sólo con su documentación | Notificar también, o en cambio, a la CM destino |
| 7 | **Si falla la notificación** | No lo dice | La recepción **no** se registra (rollback) y no se muestra el éxito. Desde la mejora O1 (`saveAndFlush`) los errores de base aparecen antes de notificar. **Riesgo residual:** si fallara el commit después de notificar, el aviso ya salió y no se puede revertir | Registrar la recepción y notificar después del commit, con reintentos o una bandeja de salida (cambia la semántica) |
| 8 | Qué significa **A6** | "El EB no confirma la selección de la opción de recepción" | Se cancela todo el registro ("Registro cancelado") y no se modifica nada | Volver a la lista de opciones |
| 9 | **Opción 4** con un destino que no es la CMC | Paso 7: se muestran las 4 opciones | Se muestran siempre; si se elige y el destino no es la CMC, 409 con mensaje | Ocultarla o deshabilitarla (prevención de errores) |
| 10 | **Operaciones del State de otros CU** | El diagrama de clases declara 13 operaciones en el State | Sólo están las 4 del CU 28. Las demás pertenecen a otros CU | Declararlas en el abstracto con rechazo por defecto y delegarlas en `Documentacion`, sin implementar otros CU |
| 11 | Cómo presentar las **extensiones** en los diagramas | — | Los originales no se modificaron. Las diferencias están en `docs/diferencias_codigo_diagramas.md` | Diagramas complementarios en el PDF (`alt` A3/A4/A5, clases nuevas) o una versión actualizada |
| 12 | **Persistencia recreada** en cada arranque | — | `ddl-auto=create` + `data.sql`: la demo siempre empieza igual. Se puede mostrar que persiste entre reinicios con argumentos (ver la guía de demostración) | Dejarla persistente por defecto |
| 13 | **Comparación del texto de la opción** en el gestor | — | Dos cadenas `if` sobre la opción elegida, que no son sobre estados | Un objeto por opción (polimorfismo) |
| 14 | **Una pantalla para un EB** | — | El gestor y la pantalla guardan estado entre pedidos (singletons de Spring); los endpoints son `synchronized` | Un estado por sesión (`@SessionScope`) si hubiera varios usuarios a la vez |
