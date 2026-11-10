# Sesión 06: auditoría integral y correcciones de bajo riesgo

PPAI DSI 2026 · 3K5 · Grupo 06 · Fecha: 10/11/2026

Se hizo una auditoría de cumplimiento funcional, consistencia con el modelado, calidad del diseño y
cumplimiento académico de la Entrega 2. Después se aplicaron las correcciones de bajo riesgo
O1, O2 y O3. Los diagramas originales no se modificaron.

## 1. Comunicado docente (prevalece sobre las consignas generales)

> "Para la 2da entrega del PPAI no se pedirá el apartado correspondiente a Diseño de experiencia
> de usuario."

Consecuencias:
- El moodboard, el mapa de empatía, el user journey, las fichas de patrones de GUI y la
  documentación formal de UX **no aplican** a la Entrega 2. El PDF no incluye un apartado de UX.
- En la auditoría, el hallazgo **C2 (UX incompleta) pasa de crítico a NO APLICA.**
- Igual se quiere un front profesional, moderno, consistente e intuitivo, sin cambiar su
  funcionamiento. Es una mejora **opcional** y queda para otra sesión.

## 2. Matriz de hallazgos (corregida)

| # | Hallazgo | Severidad | Estado al cierre de la sesión |
|---|---|---|---|
| C1 | Falta el PDF de la Entrega 2 (carátula, patrón, vistas, detalles de implementación), sin UX | Crítico | Pendiente (grupo) |
| C2 | UX formal incompleta | ~~Crítico~~ | **NO APLICA** (comunicado docente) |
| C3 | No se puede verificar en el repositorio el texto de identificación y justificación del patrón | Crítico | Pendiente: confirmarlo en el borrador del PDF |
| I1 | La secuencia rediseñada no coincide en parámetros (A3/A4/A5) | Importante | Documentado en `docs/diferencias_codigo_diagramas.md`. Faltan los diagramas complementarios del PDF |
| I2 | El State declara 4 de las 13 operaciones del diagrama de clases | Importante | Decisión aparte (no se implementó, como se pidió) |
| I3 | La notificación salía antes del flush | Importante | **Corregido (O1)**, con riesgo residual documentado |
| I4 | Documentación operativa desactualizada y sin guía para A5 | Importante | **Corregido (O2)** |
| I5 | La persistencia se recrea en cada arranque | Importante | Documentado: comando para mostrarla entre reinicios (verificado) |
| I6 | AGENTS.md regla 1 contra CONTEXTO_IA ("no se cambian") | Importante | Pendiente (grupo) |
| I7 | El código está sólo en esta copia local | Importante | Pendiente: integrarlo al repositorio del grupo |
| M1-M10 | 8 parámetros, `if` sobre la opción, CMC por nombre, singletons con estado, `ManejadorErrores`, reintento tras una falla, opción 4 visible, nulos en los estados de remito, pruebas HTTP, erratas del diagrama | Menor | Sin cambios. Los que requieren criterio están en `docs/decisiones_para_la_tutora.md` |

## 3. O1: escribir en la base antes de notificar

**Problema.** `bolsinRepositorio.save(...)` hace un *merge* de entidades que estaban fuera de la
transacción. Hibernate inserta enseguida los cambios de estado nuevos, porque la clave es
`IDENTITY`, pero deja los UPDATE para el flush del commit, que ocurre **después** de
`llamarCasoUso29()`:
- el estado actual de la documentación;
- el estado de los remitos;
- el cierre de los cambios de estado anteriores.

Un error de SQL en esos UPDATE aparecería con la notificación ya emitida.

**Evidencia.** La prueba nueva `cuandoSeNotificaLosCambiosYaEstanEscritosEnLaBase` consulta la
base desde dentro de la notificación, en la misma transacción y conexión. Antes del cambio
**falló**: vio `EnBolsinEnviado` y `Enviado`. Después del cambio da bien: ve `RecibidaYAceptada`
y `RecibidoYAceptado`.

**Cambio.** `GestorRegistroRecepcionBolsines.actualizarBolsin()` ahora usa
`bolsinRepositorio.saveAndFlush(...)`, una línea más el comentario.

**Lo que resuelve y lo que no:**
- ✅ Cualquier error de base (restricciones, bloqueos, SQL) aparece **antes** de notificar. La
  transacción se revierte, el gestor descarta el registro en curso y no sale ninguna
  notificación.
- ❌ Si el **commit** fallara después del flush (por ejemplo, por un corte de conexión), la
  notificación ya salió y no se puede revertir. Cubrir eso exige notificar después del commit
  (`@TransactionalEventListener(AFTER_COMMIT)`, con reintentos o una bandeja de salida). Eso
  cambia la semántica acordada: hoy, si falla la notificación, no se registra la recepción.
  Quedó como **decisión 7** en `docs/decisiones_para_la_tutora.md`. Con la implementación de log
  actual el riesgo no tiene efecto práctico.

## 4. O2: documentación al día

- `PENDIENTE.txt`:
  - las tareas 27 y 28 completas;
  - la tarea 25 con 24 pruebas;
  - los entregables de UX (29 y 30) como **N/A por comunicado docente**, no como completados;
  - la tarea 31, el PDF, sin UX;
  - tareas nuevas: 21b (diagramas complementarios), 27b, 28b y 30b (mejora visual opcional);
  - las decisiones abiertas actualizadas.
- `README.md`: los flujos implementados, la estructura con `notificacion/`, los fixtures y los
  documentos nuevos, `npm ci` y las 24 pruebas.
- `COMO_EJECUTAR.txt`:
  - A3 a A6 descriptos;
  - el comando para A5 con el fixture;
  - la persistencia entre reinicios;
  - `JAVA_HOME` sólo para la terminal y `npm ci`;
  - las 24 pruebas y las referencias corregidas.
- `CONTEXTO_IA.md`: el resumen del estado, la UX como no aplica, el State con
  `RecibidaYRechazada`, los fixtures, el estado actual (tabla), el enlace a las diferencias y los
  pendientes reales.
- **Documentos nuevos:**
  - `GUIA_DEMOSTRACION.md`: guion de la defensa, con A5 en la Parte 2, y el resultado esperado de
    cada paso en el front, el log y la base;
  - `docs/diferencias_codigo_diagramas.md`: todas las diferencias, clasificadas en necesarias,
    justificadas, erratas y no implementadas;
  - `docs/decisiones_para_la_tutora.md`: 14 decisiones provisionales.
- Se agregó una nota de actualización al principio de la sesión 01.

## 5. O3: A1 y A2 en el front de esta computadora

| Prueba | Resultado |
|---|---|
| Precinto `9999` | "No se encontró un bolsín con el número de precinto "9999"." ✅ |
| Precinto `5002` | Sólo aparece el 5002 ✅ |
| CM de origen `Salta` | "No se encontró un bolsín de la comisión médica origen "Salta"." ✅ |
| CM de origen `mendoza` (minúsculas) | Sólo aparece el 5005 ✅ |

## 6. Verificación

- `mvnw -o clean install` con JDK 21: **24 pruebas, 0 fallas**. Son las 23 anteriores más la
  prueba de O1.
- **Persistencia entre reinicios:** se registró la recepción del 5002, se reinició el back con
  `--spring.jpa.hibernate.ddl-auto=update --spring.sql.init.mode=never` y el 5002 siguió
  recibido.

## 7. Pendientes reales

**El grupo:**
- el PDF de la Entrega 2, sin UX (C1, C3);
- los diagramas complementarios (I1);
- unificar la regla sobre los diagramas (I6);
- integrar la copia al repositorio (I7);
- ensayar la defensa con la guía.

**Decisiones aparte:** las 9 operaciones del State de otros CU (I2).

**La tutora:** los 14 puntos de `docs/decisiones_para_la_tutora.md`.

**Opcional:** la mejora visual del front, y los hallazgos menores M5, M6, M7 y M8.
