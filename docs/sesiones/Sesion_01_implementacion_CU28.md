# Sesión 01: implementación del CU 28 "Registrar recepción de bolsín"

PPAI DSI 2026 · 3K5 · Grupo 06 · Fecha: 09/11/2026

> **Nota (10/11/2026).** Este documento registra el estado al 09/11. Desde entonces cambiaron:
> - las firmas de los mensajes 46 a 52 (`actualizarEstado` y `actualizarDocumentacion` reciben la
>   documentación marcada en A3, A4 y A5);
> - el mensaje 66 (el CU 29 ahora recibe una `NotificacionRecepcionBolsin` por cada CM de origen);
> - `save` pasó a `saveAndFlush`;
> - las pruebas (ahora son 24).
>
> Las diferencias vigentes están en `docs/diferencias_codigo_diagramas.md` y el estado actual, en
> `CONTEXTO_IA.md`. `CONTEXTO_IA.pdf` ya no existe, y `PENDIENTE.txt` está en la raíz.

Este documento explica qué se construyó en esta sesión, cómo funciona cada parte del código y a
qué mensaje del diagrama de secuencia (`docs/diagramas/SECUENCIA PATRON.pdf`) corresponde cada
línea importante. Sirve para estudiar para la defensa.

---

## 1. Qué se hizo en la sesión

1. **Estructura base** tomando como modelo el repositorio de la profesora
   (`LiberaeMentis/dsi-implementaciones-uc`):
   - back en Java 21 + Spring Boot;
   - front en React + Vite + MUI.
2. **Persistencia:** primero se armó con PostgreSQL en Docker. Después se cambió a **H2 en
   archivo** (`back/data/bolsines.mv.db`), porque para un solo CU Docker no aporta nada y
   complicaba el arranque. Ningún documento de la cátedra pide microservicios, y el repositorio
   de la profesora tampoco está dividido en microservicios. Por eso es una sola aplicación en
   capas.
3. **Revisión de los diagramas** de clases, secuencia y máquina de estados contra las consignas.
   Se agregaron parámetros y retornos, y se aplicó el patrón State sobre Documentación. Los
   puntos 5 y 6 de la revisión quedaron como estaban, por decisión del grupo:
   - el `*` representa un loop;
   - la transición RecibidaYRechazada → ParaRedirigir se mantiene.
4. **Implementación** del flujo básico del CU 28 (back y front), con pruebas del patrón State.
5. **Problemas de compilación en la PC, resueltos:**
   - Maven tomaba el JDK 27 y Lombok fallaba con `EndPosTable`. Se fijó `JAVA_HOME` a
     temurin-21.
   - Faltaba el wrapper `mvnw`.
   - El antivirus cortaba el SSL con Maven Central.
6. **Pasada de consistencia** (lo último de la sesión). Se recorrió el diagrama de secuencia
   mensaje por mensaje y se ajustó el código para que cada flecha sea una llamada real. El
   diagrama no se tocó: el código se adapta al diagrama. El detalle está en la sección 4 y las
   diferencias que quedan, en la sección 9.

## 2. Cómo se ejecuta

| Qué | Comando (desde la carpeta del proyecto) | Dónde se ve |
|---|---|---|
| Back | `cd back` → `.\mvnw.cmd spring-boot:run` (con `JAVA_HOME` en JDK 21) | http://localhost:8080/actuator/health |
| Front | `cd front` → `npm run dev` | http://localhost:3000 |
| Base de datos | Consola H2 con el back levantado | http://localhost:8080/h2-console (URL `jdbc:h2:file:./data/bolsines`, usuario `sa`, sin contraseña) |
| Pruebas | `cd back` → `.\mvnw.cmd clean install` | 8 pruebas de `EstadoDocumentacionTest` |

Para cortar cualquiera de los dos: **Ctrl + C** en su terminal (y `S` si pregunta).

Cada vez que arranca, el back recrea las tablas y vuelve a cargar `data.sql`, así que los datos
de prueba siempre empiezan igual.

## 3. Arquitectura: de las clases del diseño al código

| Diseño (cátedra) | Código | Paquete |
|---|---|---|
| «boundary» PantallaRegistroRecepcionBolsines | `@RestController`: un endpoint por cada mensaje del actor, con el mismo nombre; los `mostrarX()` son métodos de la pantalla | `pantalla` |
| «control» GestorRegistroRecepcionBolsines | `@Service`: coordina el CU y recuerda lo que el actor eligió (Fabricación Pura, Controlador) | `gestor` |
| «entity» Sesion, Usuario, Empleado, ComisionMedica, Bolsin, CambioEstadoBolsin, EstadoBolsin, Remito, DetalleRemito, EstadoRemito, Documentacion, CambioEstadoDocumentacion, TipoDocumento | Entidades JPA con su comportamiento (Experto) | `modelo.*` |
| EstadoDocumentacion + 9 estados concretos (State) | Clase abstracta + subclases | `modelo.documentacion` |
| CU 29 Notificar recepción de bolsín («include») | `InterfazNotificacionBolsin` + `NotificacionBolsinLog` (RNF 5: se puede cambiar por Gmail u Outlook) | `notificacion` |
| (diseño de persistencia) | Repositorios Spring Data JPA | `repositorio` |
| (datos que van y vienen de la pantalla) | Records `...Request` / `...Response`: nunca se devuelve una entidad | `dto` |
| La pantalla dibujada | React: un paso del wizard por cada parte del diálogo | `front/src` |

### Cómo viaja un mensaje

```
EB (front React) --POST /api/registro-recepcion-bolsin/<mensaje>--> Pantalla (@RestController)
Pantalla --mensaje--> Gestor (@Service) --mensajes--> entidades (modelo)
Gestor --mostrarX(...)--> Pantalla        (la pantalla guarda lo que tiene que mostrar)
Pantalla --respuesta JSON--> front, que lo dibuja
```

La pantalla y el gestor se conocen mutuamente, como en el diagrama: la pantalla le habla al
gestor y el gestor le contesta con `mostrarX()`. Spring no puede crear dos beans que se
necesitan entre sí en el constructor. Por eso el gestor recibe la pantalla con `@Lazy`: Spring le
pasa un "representante" que busca la pantalla real recién cuando se usa. `back/lombok.config`
hace que Lombok copie ese `@Lazy` al constructor.

La pantalla y el gestor guardan estado entre pedidos, porque representan la pantalla de un EB.
Por eso los 4 endpoints de la pantalla son `synchronized`. Antes, si llegaban dos pedidos al
mismo tiempo, la lista de bolsines se mezclaba o aparecía vacía. Pasaba, por ejemplo, cuando
React en modo desarrollo llamaba dos veces a `opcRegistrarRecepcionBolsin` al abrir la página.

## 4. Recorrido del diagrama de secuencia contra el código

Notación: `Gestor` = GestorRegistroRecepcionBolsines, `Pantalla` =
PantallaRegistroRecepcionBolsines. "self" = mensaje a sí mismo (método privado).

### 4.1 Inicio: el EB elige la opción

| # | Mensaje del diagrama | Código | Qué hace |
|---|---|---|---|
| 1 | EB → Pantalla `opcRegistrarRecepcionBolsin()` | `Pantalla.opcRegistrarRecepcionBolsin()` (POST `/opcRegistrarRecepcionBolsin`) | Endpoint que llama el front al abrir la pantalla |
| 2 | Pantalla self `habilitarPantalla()` | `Pantalla.habilitarPantalla()` | Limpia lo que se mostraba antes |
| 3 | Pantalla → Gestor `opcRegistrarRecepcionBolsin()` | `Gestor.opcRegistrarRecepcionBolsin()` | Arranca el CU; antes llama a `finCU()` para descartar un registro anterior sin terminar |
| 4 | Gestor self `buscarUsuarioLogueado()` | `Gestor.buscarUsuarioLogueado()` | Ver mensajes 5 a 7 |
| 5 | Gestor → Activa:Sesion `getUsuario(): Usuario` | `sesionRepositorio.findFirstByFechaHoraFinIsNull()` → `Sesion.getUsuario()` | La sesión activa es la que no tiene `fechaHoraFin` |
| 6 | Sesion → Usuario `getUsuario(): Usuario` | `Usuario.getUsuario()` (devuelve `this`) | Está porque el diagrama lo muestra |
| 7 | Gestor → Empleado `*esTuUsuario(u)` | `for` sobre los empleados + `Empleado.esTuUsuario(usuario)` | El `*` es el loop: encuentra al EB logueado |
| 8 | Gestor self `buscarCMUsuario()` | `Gestor.buscarCMUsuario()` | Guarda `cmUsuario` |
| 9 | Gestor → Logueado:Empleado `obtenerCM(): ComisionMedica` | `Empleado.obtenerCM()` | Devuelve la comisión médica del EB |
| 10 | Empleado → ComisionMedica `getNombre()` | `cmUsuario.getNombre()` (lo envía el gestor; ver sección 9) | Nombre para mostrar |
| 11 | Gestor → Pantalla `mostrarCM(nombre)` | `Pantalla.mostrarCM(nombre)` | Guarda el nombre: el front lo muestra en el chip del encabezado |
| 12 | Gestor self `buscarBolsinesEnviados()` | `Gestor.buscarBolsinesEnviados()` | Loop "EstadosBolsin" sobre todos los bolsines |
| 13 | Gestor → Bolsin `buscarBolsinesEnviados(): boolean` | `Bolsin.buscarBolsinesEnviados()` | ¿Su estado actual es Enviado? |
| 14 | Bolsin → CambioEstadoBolsin `*sosActual()` | `for` + `CambioEstadoBolsin.sosActual()` | Actual = sin `fechaHoraFin` |
| 15 | Bolsin → Actual:CambioEstadoBolsin `sosEnviado()` | `CambioEstadoBolsin.sosEnviado()` | Delega en su estado |
| 16 | Actual → EstadoBolsin `sosEnviado()` | `EstadoBolsin.sosEnviado()` | Experto: el estado sabe su nombre |
| 17 | Gestor → Bolsin `esCmDestino(cm)` | `Bolsin.esCmDestino(cmUsuario)` | ¿Va a la CM del EB? |
| 18 | Gestor → Bolsin `getNumPrecinto(): int` | `Bolsin.getNumPrecinto()` | Arma `nrosPrecinto[]` |
| 19 | Gestor → Bolsin `getOrigen(): ComisionMedica` | `Bolsin.getOrigen()` | CM de origen |
| 20 | Bolsin → ComisionMedica `getNombre()` | `getOrigen().getNombre()` (ver sección 9) | Arma `origenes[]` |
| 21 | Gestor → Pantalla `mostrarBolsinesEnviadosParaSeleccion(nroPrecinto[], origen[])` | `Pantalla.mostrarBolsinesEnviadosParaSeleccion(...)` | Une los dos arreglos en la lista del combo |

Respuesta al front: `{ nombreCM, bolsines: [{ nroPrecinto, origen }] }`. Con los datos de prueba
aparecen los precintos 5001, 5002 y 5005. No aparecen el 103, que está Cerrado, ni el 104, que
va a Rosario.

### 4.2 El EB selecciona un bolsín

| # | Mensaje del diagrama | Código | Qué hace |
|---|---|---|---|
| 22 | EB → Pantalla `tomarSeleccionBolsin(nroPrecinto)` | POST `/tomarSeleccionBolsin` `{nroPrecinto}` | — |
| 23 | Pantalla → Gestor `tomarSeleccionBolsin(nroPrecinto)` | `Gestor.tomarSeleccionBolsin(int)` | Busca el bolsín entre los que mostró (si no está: 400) |
| 24 | Gestor self `buscarRemitos()` | `Gestor.buscarRemitos()` | Guarda `nrosRemito` |
| 25 | Gestor → selec:Bolsin `buscarRemitos()` | `Bolsin.buscarRemitos(): int[]` | — |
| 26 | Bolsin → Remito `*getNumero(): int` | `Remito::getNumero` dentro de `Bolsin.buscarRemitos()` | El bolsín le pide el número a cada remito |
| 27 | Gestor self `buscarDocumentacion()` | `Gestor.buscarDocumentacion()` | Guarda `documentacion` |
| 28 | Gestor → selec:Bolsin `buscarDocumentacion(): String[][]` | `Bolsin.buscarDocumentacion()` | Loop "Remitos del Bolsín" |
| 29 | Bolsin → Remito `buscarDatosDocumentacion(): String[][]` | `Remito.buscarDatosDocumentacion()` | Loop "Documentación Remito"; una fila `[nroRemito, asunto, tipo]` por documentación |
| 30 | Remito → DetalleRemito `buscarDatosDocumentacion(): String[]` | `DetalleRemito.buscarDatosDocumentacion()` | `[asunto, tipo]` |
| 31 | DetalleRemito → Documentacion `getAsunto()` | `Documentacion.getAsunto()` | — |
| 32 | DetalleRemito → Documentacion `getNombreTipo()` | `Documentacion.getNombreTipo()` | — |
| 33 | Documentacion → TipoDocumento `getNombre()` | `TipoDocumento.getNombre()` | — |
| 34 | Gestor → Pantalla `mostrarNumRemitosyDocumentacionAsociada(nrosRemito[], documentacion[][])` | `Pantalla.mostrarNumRemitosyDocumentacionAsociada(...)` | Agrupa las filas por remito (`RemitoConDocumentacion.agrupar`) |
| 35 | Gestor self `getOpcionesRecepcion()` | `Gestor.getOpcionesRecepcion()` | Las 4 opciones del ERS |
| 36 | Gestor → Pantalla `mostrarOpcionesRecepcionParaSeleccion(opciones[])` | `Pantalla.mostrarOpcionesRecepcionParaSeleccion(...)` | — |

Respuesta al front: `{ remitos: [{ nroRemito, documentacion: [{ asunto, tipo }] }], opciones }`.

### 4.3 El EB elige la opción de recepción

| # | Mensaje del diagrama | Código | Qué hace |
|---|---|---|---|
| 37 | EB → Pantalla `tomarSeleccionOpcionRecepcion(opcion)` | POST `/tomarSeleccionOpcionRecepcion` `{opcion}` | `@NotBlank`: vacío da 400 |
| 38 | Pantalla → Gestor `tomarSeleccionOpcionRecepcion(opcion)` | `Gestor.tomarSeleccionOpcionRecepcion(String)` | Valida la opción. Sólo la opción 1 ("El contenido del bolsín es igual al registrado") está implementada; las otras dan 400 con un mensaje |
| 39 | Gestor → Pantalla `solicitarConfirmacionRegistro()` | `Pantalla.solicitarConfirmacionRegistro()` | Mensaje "¿Confirma el registro de la recepción del bolsín?" |

### 4.4 El EB confirma: actualización (acá aparece el State)

| # | Mensaje del diagrama | Código | Qué hace |
|---|---|---|---|
| 40 | EB → Pantalla `tomarConfirmacionRegistro(confirmacion)` | POST `/tomarConfirmacionRegistro` `{confirmacion}` | — |
| 41 | Pantalla → Gestor `tomarConfirmacionRegistro(confirmacion)` | `Gestor.tomarConfirmacionRegistro(boolean)` (`@Transactional`) | Con `false` sigue el flujo A6 (sección 8) |
| 42 | Gestor self `actualizarBolsin()` | `Gestor.actualizarBolsin()` | — |
| 43 | Gestor → EstadoBolsin `*esRecibidoEnCMDestino()` | loop + `EstadoBolsin.esRecibidoEnCMDestino()` | Puntero al estado nuevo del bolsín |
| 44 | Gestor → EstadoRemito `*esRecibidoyAceptado()` | loop + `EstadoRemito.esRecibidoyAceptado()` | Puntero al estado nuevo de los remitos |
| 45 | Gestor self `getFechaHoraActual(): datetime` | `Gestor.getFechaHoraActual()` (`LocalDateTime.now(reloj)`) | La hora sale del bean `Clock` |
| 46 | Gestor → selec:Bolsin `actualizarEstado(fhActual, estadoBolsin, estadoRemito, responsable)` | `Bolsin.actualizarEstado(...)` | Después de esto, `bolsinRepositorio.save(...)` guarda todo en cascada (persistencia) |
| 47 | Bolsin self `actualizarRemitos(fhActual, estadoRemito, responsable)` | `Bolsin.actualizarRemitos(...)` (privado) | Loop "Actualizar Estado Remitos" |
| 48 | Bolsin → Remito `actualizarEstado(estadoRemito, fhActual, responsable)` | `Remito.actualizarEstado(...)` | — |
| 49 | Remito self `setEstado(e)` | `Remito.setEstado(e)` | El remito pasa a RecibidoYAceptado |
| 50 | Remito self `actualizarDocumentacion(fhActual, responsable)` | `Remito.actualizarDocumentacion(...)` (privado) | Loop "ActualizarEstadosDocumentacion" |
| 51 | Remito → DetalleRemito `actualizarDocumentacion(fhActual, responsable)` | `DetalleRemito.actualizarDocumentacion(...)` | — |
| 52 | DetalleRemito → Documentacion `recibirYAceptar(fhActual, responsable)` | `Documentacion.recibirYAceptar(...)` | **Contexto del State:** no decide nada, delega en su estado actual |
| 53 | Documentacion → EnBolsinEnviado `recibirYAceptar(this, historial, fhActual, responsable)` | `estadoActual.recibirYAceptar(this, cambioEstado, fhActual, responsable)` | Se pasa a sí misma y su historial |
| 54 | EnBolsinEnviado → `new(): RecibidaYAceptada` | `new RecibidaYAceptada()` | El estado concreto crea el siguiente |
| 55 | EnBolsinEnviado self `cambiarEstado(this, historial, fhActual, responsable, nuevo)` | `EstadoDocumentacion.cambiarEstado(...)` (heredado, `protected`) | Es igual para todas las transiciones; por eso está en la clase abstracta |
| 56 | EnBolsinEnviado → CambioEstadoDocumentacion `*sosActual()` | loop + `CambioEstadoDocumentacion.sosActual()` | — |
| 57 | → Actual:CambioEstadoDocumentacion `setFechaHoraFin(fhActual)` | `ce.setFechaHoraFin(fhActual)` | Cierra el cambio de estado actual |
| 58 | self `crearCambioEstado(fhInicio, nuevo, responsable): CambioEstadoDocumentacion` | `EstadoDocumentacion.crearCambioEstado(...)` | — |
| 59 | → `new(fhInicio, e, responsable)`: Nuevo:CambioEstadoDocumentacion | `new CambioEstadoDocumentacion(fhInicio, nuevo, responsable)` | Guarda quién hizo el cambio |
| 60 | EnBolsinEnviado → Documentacion `setEstado(e)` | `doc.setEstado(nuevo)` | — |
| 61 | EnBolsinEnviado → Documentacion `agregarCE(ce)` | `doc.agregarCE(ce)` | — |
| 62 | selec:Bolsin → Actual:CambioEstadoBolsin `setFechaHoraFin(fhfin)` | En `Bolsin.actualizarEstado`: loop `sosActual()` + `setFechaHoraFin(fhActual)` | Se hace después de actualizar remitos y documentación (corrección del tutor) |
| 63 | selec:Bolsin self `crearCambioEstado()` | `Bolsin.crearCambioEstado(fhInicio, e, responsable)` (privado) | — |
| 64 | → `new(fhInicio, e, usuarioResponsable)`: Nuevo:CambioEstadoBolsin | `new CambioEstadoBolsin(...)` y se agrega a `cambioEstado` | El bolsín queda RecibidoEnCMDestino |

### 4.5 Cierre

| # | Mensaje del diagrama | Código | Qué hace |
|---|---|---|---|
| 65 | Gestor self `llamarCasoUso29()` | `Gestor.llamarCasoUso29()` | — |
| 66 | Gestor → CU 29 `«include»` | `interfazNotificacion.notificarRecepcionBolsin(emailOrigen, nroPrecinto, nombreCMDestino)` | Por ahora deja el "correo" en el log: `CU 29 - Correo a ...` |
| 67 | Gestor → Pantalla `mostrarEjecucionExitosa()` | `Pantalla.mostrarEjecucionExitosa()` | Mensaje de éxito |
| 68 | Gestor self `FinCU()` | `Gestor.finCU()` | Limpia todo lo que el gestor recordaba |

## 5. El patrón State en el código

**Problema:** lo que puede hacerse con una documentación depende de su estado (máquina de
estados). Sin el patrón, Documentación tendría un `if`/`switch` por estado en cada método.

**Solución (Gamma):**

- **Contexto:** `Documentacion`.
  - Tiene `estadoActual` (EstadoDocumentacion) y su historial `cambioEstado`.
  - Sus métodos `recibirYAceptar`, `recibirYRechazar`, `registrarNoRecibida` y `redirigir` sólo
    delegan: `estadoActual.recibirYAceptar(this, cambioEstado, fhActual, responsable)`.
- **Estado abstracto:** `EstadoDocumentacion`, una clase abstracta y no una interfaz.
  - Declara las cuatro transiciones del CU 28. Por defecto **rechazan** la operación con
    `IllegalStateException("No se puede ... una documentación en estado X")`, que la API devuelve
    como 409.
  - Tiene el código común `cambiarEstado(...)` y `crearCambioEstado(...)`. Reusar ese código es lo
    que justifica la herencia.
- **Estados concretos:** Registrada, EnRemito, EnBolsinSaliente, EnBolsinEnviado,
  RecibidaYAceptada, RecibidaYRechazada, NoRecibida, ParaRedirigir y DeBaja.
  - Cada uno redefine sólo las transiciones que salen de él.
  - `EnBolsinEnviado` redefine las cuatro: va a RecibidaYAceptada, RecibidaYRechazada, NoRecibida
    o ParaRedirigir.
  - `ParaRedirigir` redefine `recibirYAceptar` y `recibirYRechazar`, porque el área destino recibe
    la documentación redirigida.
  - Las transiciones de otros CU (`enviar`, `darDeBaja`, etc.) no se implementan: el enunciado
    pide sólo el CU 28.

```java
// EnBolsinEnviado: crea el estado siguiente y usa el código común heredado
public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
        LocalDateTime fhActual, Empleado responsable) {
    cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYAceptada());
}

// EstadoDocumentacion (abstracta)
protected void cambiarEstado(Documentacion doc, List<CambioEstadoDocumentacion> historial,
        LocalDateTime fhActual, Empleado responsable, EstadoDocumentacion nuevo) {
    for (CambioEstadoDocumentacion ce : historial)
        if (ce.sosActual()) ce.setFechaHoraFin(fhActual);   // cierra el actual
    CambioEstadoDocumentacion ce = crearCambioEstado(fhActual, nuevo, responsable);
    doc.setEstado(nuevo);
    doc.agregarCE(ce);
}
```

**Cómo se guarda el estado en la base (diseño de persistencia):**

- En la tabla `documentacion`, la columna `estado_actual` guarda el **nombre** del estado (por
  ejemplo `EnBolsinEnviado`). Lo mismo pasa en `cambio_estado_documentacion.estado`.
- `EstadoDocumentacionConverter` (JPA `AttributeConverter`) hace la conversión:
  - al guardar: `estado.getNombre()`;
  - al leer: busca el nombre en un mapa y crea el objeto concreto (`new EnBolsinEnviado()`).
- Así no hace falta una tabla por estado, y el polimorfismo funciona igual al leer de la base.

**Pruebas** (`EstadoDocumentacionTest`, 8 pruebas, sin base de datos, con Mockito para el
Empleado):

- las 4 transiciones desde EnBolsinEnviado;
- ParaRedirigir acepta;
- un estado sin la transición la rechaza y no cambia nada;
- ParaRedirigir no se puede volver a redirigir;
- el convertidor guarda el nombre y reconstruye el estado concreto.

## 6. Persistencia

- **H2 en archivo** (`spring.datasource.url=jdbc:h2:file:./data/bolsines`).
  - `ddl-auto=create` hace que Hibernate cree las tablas a partir de las entidades.
  - `data.sql` carga los datos de prueba.
  - `defer-datasource-initialization` hace que los datos se carguen después de crear las tablas.
- **Mapeos importantes:**
  - Colecciones `@OneToMany(fetch = EAGER)` + `@Fetch(SUBSELECT)`. El gestor guarda entidades
    entre un request y otro, así que tienen que venir cargadas completas. SUBSELECT evita el
    problema de N+1 consultas.
  - Cascadas:
    - `Bolsin → CambioEstadoBolsin`: ALL;
    - `Bolsin → Remito`: MERGE;
    - `Remito → DetalleRemito`: ALL;
    - `DetalleRemito → Documentacion`: MERGE;
    - `Documentacion → CambioEstadoDocumentacion`: ALL.

    Con esas cascadas, un solo `bolsinRepositorio.save(bolsin)` guarda el bolsín, sus remitos,
    la documentación y todos los cambios de estado nuevos.
  - `@Transactional` en `tomarConfirmacionRegistro`: si algo falla, no se guarda nada a medias.
- **Datos de prueba** (`data.sql`):
  - EB logueada: Laura Gómez, Comisión Médica Córdoba (sesión activa id 2).
  - Bolsines:

    | Bolsín | Precinto | Origen → Destino | Estado | ¿Aparece? |
    |---|---|---|---|---|
    | 101 | 5001 | Central → Córdoba | Enviado | Sí (remitos 1001 y 1002, 3 documentaciones) |
    | 102 | 5002 | Rosario → Córdoba | Enviado | Sí |
    | 103 | 5003 | Mendoza → Córdoba | Cerrado | No |
    | 104 | 5004 | Central → Rosario | Enviado | No (es de otra CM) |
    | 105 | 5005 | Mendoza → Córdoba | Enviado | Sí |

## 7. Front (React)

`front/src/App.jsx` es la pantalla. Es un wizard de 5 pasos y cada paso llama al endpoint del
mensaje correspondiente (`services/api.js` tiene una función por mensaje, con el mismo nombre).

| Paso | Componente | Mensaje que dispara | Qué muestra |
|---|---|---|---|
| Al abrir | — | `opcRegistrarRecepcionBolsin` | CM del EB (chip del encabezado) |
| 1 | `SeleccionBolsin.jsx` | `tomarSeleccionBolsin` | Tabla de bolsines enviados, con filtro por precinto y CM origen |
| 2 | `ContenidoBolsin.jsx` | — | Remitos y documentación del bolsín elegido |
| 3 | `OpcionRecepcion.jsx` | `tomarSeleccionOpcionRecepcion` | Las 4 opciones de recepción |
| 4 | `Confirmacion.jsx` | `tomarConfirmacionRegistro(true/false)` | Resumen y botones Confirmar / No confirmar |
| 5 | `Resultado.jsx` | — | Mensaje de éxito o de cancelación |

El botón **Cancelar** del encabezado vuelve a empezar: llama otra vez a
`opcRegistrarRecepcionBolsin`, y el gestor descarta lo anterior con `finCU()`.

Vite redirige `/api` al back (puerto 8080), así que el front no tiene la URL del back escrita
en el código.

## 8. Flujos alternativos implementados

- **A1 / A2**: el EB filtra por precinto o CM de origen y no hay coincidencias. Lo resuelve el
  front con un aviso.
- **A6**: el EB no confirma. Se llama a `tomarConfirmacionRegistro(false)` y el gestor:
  1. llama a `pantalla.mostrarRegistroCancelado()`;
  2. llama a `finCU()`;
  3. termina sin tocar el bolsín.

  `mostrarRegistroCancelado()` no está en el diagrama porque es un flujo alternativo.
- **Errores**: `ManejadorErrores` (`@RestControllerAdvice`) traduce las excepciones a un mensaje:
  - 400 para un dato inválido;
  - 409 para una operación que no corresponde en ese momento.
- **Pendiente decidir** (tarea 27):
  - A3: no llega toda la documentación.
  - A4: hay documentación que no corresponde al destino.

  Mostrarían el polimorfismo del State (otros estados destino), pero el diagrama no los modela.

## 9. Diferencias que quedan entre el código y los diagramas

Son detalles chicos. Conviene conocerlos para la defensa o corregirlos en el diagrama si el
grupo lo decide.

| Dónde | Diagrama | Código | Por qué |
|---|---|---|---|
| `obtenerCM()` y `getOrigen()` | La flecha `getNombre()` sale de Empleado / Bolsín hacia ComisionMedica | El gestor recibe la ComisionMedica y le envía `getNombre()` | El método devuelve `ComisionMedica`, como dice el diagrama. El gestor necesita el objeto CM (para `esCmDestino`) y además el nombre (para `mostrarCM`), y un método no puede devolver las dos cosas. El gestor ya conoce a ComisionMedica, así que no se agrega acoplamiento |
| `Bolsin.buscarRemitos()` | Retorna `remito[]` | Retorna `int[]` | Se respeta la flecha `*getNumero()` de Bolsín a Remito: el bolsín arma los números y el gestor no se acopla a Remito (Bajo Acoplamiento) |
| `Bolsin.crearCambioEstado()` | Sin parámetros | `crearCambioEstado(fhInicio, e, responsable)` | El `new(fhInicio, e, usuarioResponsable)` necesita esos datos, que son parámetros de `actualizarEstado` |
| Cierre del CE actual del bolsín | `setFechaHoraFin` directo a Actual:CambioEstadoBolsin | Primero busca el actual con `sosActual()` | Es la misma búsqueda que el diagrama dibuja en `buscarBolsinesEnviados` |
| `getOpcionesRecepcion()` | `void` en el diagrama de clases | Devuelve `String[]` | Se pasa directo a `mostrarOpcionesRecepcionParaSeleccion` |
| Orden del «include» | `mostrarEjecucionExitosa()` antes del «include» | La notificación va dentro de `llamarCasoUso29()`, antes de mostrar el éxito | Si la notificación falla, el EB no ve un éxito falso |
| Persistencia y validaciones | No aparecen | `findAll()` en los repositorios, `save()`, controles de orden de los pasos | Pertenecen al diseño de persistencia, no a la realización del CU |

**Nombres:** el código usa los de la secuencia. Estas son las diferencias con el diagrama de
clases o los errores de tipeo que conviene corregir en los diagramas:

- `tomarConfirmaionRegistro`: falta una "c" en la flecha Pantalla → Gestor de la secuencia.
- `FinCU()` en la secuencia y `finCU()` en clases. En Java un método empieza con minúscula.
- `esCmDestino` en la secuencia y `esCMDestino` en clases.
- `recibirYAceptar` en la secuencia y `recibiryAceptar` / `recibiryRechazar` en clases.
- `mostrarNumRemitosyDocumentacionAsociada`: en clases está con "Y" mayúscula y con tilde.
- `esRecibidoyAceptado()` / `esRecibidoyAceptada()` aparecen en **EstadoBolsin** en el diagrama de
  clases. Deberían estar en **EstadoRemito**, que es a quien se lo pregunta la secuencia.
- `Bolsin.buscarBolsinesEnviados(cmUsuario)` tiene un parámetro en clases y ninguno en la
  secuencia, donde la CM se pregunta aparte con `esCmDestino`.

## 10. Verificación hecha

- **Compilación** con las mismas versiones que usa Maven en la PC: Spring Boot 4.1.1,
  Hibernate 7.4.5 y Lombok 1.18.46, tomados de la carpeta `.m2`.
- **Pruebas:** 8 de 8 pasan, con JUnit 6 y Mockito.
- **Flujo completo por HTTP:**
  - abrir;
  - elegir 5001;
  - opción no implementada (400);
  - opción 1;
  - confirmar;
  - reabrir (5001 ya no aparece);
  - A6 con 5002 (no se modifica);
  - mensaje del CU 29 en el log.
- **Base de datos después de confirmar:**
  - documentaciones 1, 2 y 3 en RecibidaYAceptada, con el cambio de estado anterior cerrado y el
    nuevo con responsable Laura Gómez;
  - remitos 1001 y 1002 en RecibidoYAceptado;
  - bolsín 101 en RecibidoEnCMDestino;
  - bolsín 102 sigue Enviado.
- **Prueba en la PC con el front real (09/11):**
  - se recibieron los 3 bolsines de Córdoba uno por uno;
  - la lista se actualizó bien en cada paso y al final mostró "No hay bolsines enviados a su
    comisión médica";
  - la base quedó con los 3 bolsines en RecibidoEnCMDestino y su documentación en
    RecibidaYAceptada.
- **Traspaso al grupo:** `CONTEXTO_IA.md` / `CONTEXTO_IA.pdf` y `COMO_EJECUTAR.txt`, en la raíz.

## 11. Pendiente

Ver `docs/PENDIENTE.txt`:

- decidir los flujos alternativos A3 y A4;
- UX (moodboard, mapa de empatía, user journey, patrones de GUI y tema);
- PDF de la Entrega 2;
- preparar la defensa;
- repositorio del grupo.
