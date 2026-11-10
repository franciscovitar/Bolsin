# Contexto del proyecto para seguir trabajando (grupo e IA)

**PPAI DSI 2026 "Bolsines" · UTN FRC · 3K5 · Grupo 06 · Estado al 10/11/2026**

> **Resumen del estado:** el CU 28 está completo: flujo descripto, los seis alternativos A1 a A6
> y el CU 29 con la situación de la documentación. Hay 25 pruebas y todas pasan. La revisión técnica y el rediseño visual están terminados. Falta:
> - el PDF de la Entrega 2, sin el apartado de UX por comunicado docente;
> - validar con la tutora las decisiones de `docs/decisiones_para_la_tutora.md`;
> - acordar cómo integrar la rama `cierre-cu28` al repositorio del grupo.

Este archivo resume todo lo necesario para retomar el trabajo desde donde quedó. Está escrito
para que cualquier integrante se lo pase a una IA (Claude, ChatGPT, Copilot, Cursor) junto con la
carpeta del proyecto, y la IA siga con las mismas reglas y decisiones.

---

**Antes de cambiar algo:** las reglas originales del grupo están en `AGENTS.md`.
Las reglas 1 y 7 no se consideran reemplazadas por el hecho de haber documentado diferencias.
Los diagramas definitivos se conservaron, pero los mensajes y parámetros agregados deben
ser revisados con el grupo. La guía `GUIA_CAMBIOS_DEL_PROYECTO.md` explica qué trabajo
ya existía y qué se agregó posteriormente.

---

## 0. Cómo usarlo

1. Cloná el repositorio del grupo (o descomprimí el .zip) y levantalo siguiendo
   `COMO_EJECUTAR.txt`.
2. Abrí una conversación con la IA y adjuntá o dejá que lea:
   - este archivo (`CONTEXTO_IA.md`);
   - `AGENTS.md` (reglas del proyecto);
   - `PENDIENTE.txt` (plan de trabajo, en la raíz);
   - `docs/sesiones/Sesion_01_implementacion_CU28.md`, que tiene la tabla mensaje por mensaje
     del diagrama de secuencia contra el código, y las sesiones 02 a 06 (A3, A4, A5, CU 29 y
     auditoría);
   - `docs/diferencias_codigo_diagramas.md` y `docs/decisiones_para_la_tutora.md`.
3. Pegá este mensaje inicial y completá la tarea:

```
Sos mi asistente para el PPAI de Diseño de Sistemas de Información (UTN FRC 2026, caso
"Bolsines"). Antes de hacer nada, leé CONTEXTO_IA.md, AGENTS.md, PENDIENTE.txt,
docs/diferencias_codigo_diagramas.md y docs/sesiones/, y respetá todas las reglas que figuran ahí
(sobre todo: los diagramas originales NO se modifican y toda diferencia se documenta; todo en
español; 100 % orientado a objetos; código corto y sin repeticiones).
Tarea de hoy: <describir la revisión o el cambio concreto que se necesita>.
Antes de escribir código, decime qué archivos vas a tocar y por qué.
```

Si usan **Claude Code** o **Cursor** dentro de la carpeta, `AGENTS.md` se puede leer solo.
Igual conviene decirle que lea este archivo.

---

## 1. El trabajo práctico

- **Materia:** Diseño de Sistemas de Información, UTN Facultad Regional Córdoba. Cátedra
  Meles / Massano.
- **Caso:** organismo de comisiones médicas (CM) que se envían documentación en **bolsines**
  por correo. Cada bolsín tiene remitos, y cada remito tiene documentación.
- **Asignación del grupo (par):**
  - **CU 28 "Registrar recepción de bolsín"**;
  - máquina de estados de **Documentación**;
  - patrón de Gamma **State aplicado a la clase Documentación**.
- **Entrega 2 (Diseño), que es la etapa actual:**
  1. Rediseño con el patrón: identificación y justificación, vista estática (clases) y vista
     dinámica (secuencia). **Ya está hecho**: son los diagramas de `docs/diagramas`.
  2. Implementación del rediseño. Se evalúa:
     - que corra el **flujo descripto + al menos 2 flujos alternativos**;
     - la **consistencia con el modelado** (pantalla, gestor, entidades, patrón);
     - la **persistencia en base de datos**;
     - la defensa oral mostrando el patrón en el código.
  3. ~~UX: moodboard, mapa de empatía, user journey y patrones de GUI.~~ **No aplica a la
     Entrega 2**, por comunicado docente: "Para la 2da entrega del PPAI no se pedirá el apartado
     correspondiente a Diseño de experiencia de usuario". Se mejoró el frontend sin cambiar la lógica del CU.
  4. PDF de la entrega: `PPAI2026_3K5_G06_E2_Diseño.pdf`, con carátula completa y **sin
     apartado de UX**. El código no se sube: se defiende.
- **Fuentes de la cátedra:** `docs/catedra/`. Ahí están la ERS v1.2 (con el CU 28 completo en
  4.1), la descripción del caso, las consignas, las consignas de UX y el modelo de dominio.

### CU 28 en una línea por paso

1. El EB elige "registrar recepción".
2. El sistema muestra la CM del usuario.
3. Lista los bolsines **Enviados** a esa CM (precinto y CM de origen, con filtro).
4. El EB elige uno.
5. El sistema muestra los remitos y la documentación (asunto y tipo).
6. Muestra 4 opciones de recepción.
7. El EB elige la **opción 1** ("el contenido es igual al registrado").
8. El sistema pide confirmación y el EB confirma.
9. El sistema actualiza:
   - bolsín → RecibidoEnCMDestino;
   - remitos → RecibidoYAceptado;
   - documentación → RecibidaYAceptada, con el empleado responsable.
10. Incluye el **CU 29** (notificar a la CM de origen) y fin.

**Flujos alternativos:**

| | Descripción |
|---|---|
| A1 | No se encuentra el precinto |
| A2 | No se encuentra la CM de origen |
| A3 | Opción 2: no llega toda la documentación |
| A4 | Opción 3: hay documentación que no corresponde al destino |
| A5 | Opción 4: hay que redirigir documentación a un área (sólo si la CM destino es la Central) |
| A6 | El EB no confirma |

---

## 2. Reglas del equipo (no negociables)

1. **Los diagramas definitivos no se modifican.** El código debe poder contrastarse con ellos;
   las diferencias se registran y deben justificarse, no ocultarse. Son una entrega
   pasada con correcciones encima. Los mensajes del flujo descripto se contrastan con
   `docs/diagramas/SECUENCIA PATRON.pdf`. Hay diferencias de nombres, firmas y emisores
   entre los diagramas y el código. Además, A3, A4, A5 y CU 29 incorporaron mensajes nuevos.
   Todo eso se detalla en `docs/diferencias_codigo_diagramas.md` y queda para revisar con
   la tutora. No debe presentarse como una coincidencia exacta.
2. Convenciones de la cátedra:
   - Pantalla = «boundary»;
   - Gestor = «control», que es una Fabricación Pura;
   - entidades con comportamiento (Experto);
   - CambioEstado con `fechaHoraInicio`/`fechaHoraFin` (el actual no tiene fin) y responsable;
   - GRASP: Experto, Creador, Controlador, Bajo Acoplamiento y Alta Cohesión.
3. **Todo en español:** clases, métodos, variables, mensajes al usuario y commits.
4. **100 % orientado a objetos.** El código es corto y sin repeticiones: nada de `if`/`switch`
   sobre el nombre de un estado (para eso está el State). Las entidades nunca se devuelven a la
   pantalla: se usan records en `dto`.
5. Inyección por constructor. La hora sale del bean `Clock`.
6. Antes de dar algo por terminado, compilar y probar el flujo en el front.
7. No pegar contraseñas ni claves en el chat con la IA.
8. Respetar la estructura del repositorio de ejemplo de la cátedra
   (`github.com/LiberaeMentis/dsi-implementaciones-uc`). Mapeo:
   - pantalla = `@RestController` con un endpoint por cada mensaje del actor, con el mismo
     nombre;
   - gestor = `@Service`.

Detalle completo: `AGENTS.md`.

---

## 3. Tecnología y cómo correrlo

| Parte | Tecnología |
|---|---|
| Back | Java 21 · Spring Boot 4.1.1 · Spring Data JPA / Hibernate 7 · Lombok · Maven (wrapper `mvnw`) |
| Base de datos | H2 en modo archivo (`back/data/bolsines.mv.db`). Se recrea en cada arranque desde `data.sql` |
| Front | React 19 · Vite 7 · Material UI 7 · Axios. Proxy de `/api` a `:8080` |
| Pruebas | JUnit + Mockito + Spring Boot Test: 25 pruebas (State, DTO, texto del CU 29 y 14 de integración del CU completo contra una H2 en memoria) |

**Requisitos:** JDK **21** y Node.js 22 LTS. El paso a paso completo para una computadora
nueva (instalación, prueba del CU, consola H2 y problemas comunes) está en
**`COMO_EJECUTAR.txt`**.

```powershell
# Back -> http://localhost:8080   (salud: /actuator/health, base: /h2-console)
cd back
.\mvnw.cmd spring-boot:run          # compilar y probar: .\mvnw.cmd clean install

# Front (otra terminal) -> http://localhost:3000
cd front
npm ci
npm run dev
```

Para cortar cualquiera de los dos: Ctrl + C (y `S` si pregunta).

Para entrar a la consola H2 (`/h2-console`):
- JDBC URL: `jdbc:h2:file:./data/bolsines`
- usuario: `sa`
- contraseña: vacía

### Problemas que ya aparecieron y su solución

- **`ExceptionInInitializerError ... EndPosTable` al compilar:** Maven está usando un JDK más
  nuevo que el 21 y Lombok falla. Hay que apuntar `JAVA_HOME` a un JDK 21:
  ```powershell
  $env:JAVA_HOME = "C:\ruta\al\jdk-21"
  ```
- **IntelliJ marca dependencias sin resolver o tira "SSL peer shut down":**
  - el antivirus (por ejemplo, el escaneo HTTPS de AVG) corta la descarga. Hay que
    desactivarlo un momento o compilar una vez con `mvnw`;
  - en IntelliJ, usar el Maven wrapper y elegir JDK 21 tanto en "JDK for importer" como en
    "Runner".
- **`mvnw` no se reconoce:** los comandos se corren desde la carpeta `back`.

---

## 4. Arquitectura y dónde está cada cosa

Es una sola aplicación en capas, no microservicios: ningún documento de la cátedra los pide y
el repositorio de ejemplo tampoco los usa.

```
back/src/main/java/ppai/bolsines/
├── pantalla/      PantallaRegistroRecepcionBolsines (@RestController, «boundary») + ManejadorErrores
├── gestor/        GestorRegistroRecepcionBolsines (@Service, «control»)
├── modelo/
│   ├── usuario/         Sesion, Usuario, Empleado
│   ├── comisionmedica/  ComisionMedica
│   ├── bolsin/          Bolsin, CambioEstadoBolsin, EstadoBolsin
│   ├── remito/          Remito, DetalleRemito, EstadoRemito
│   └── documentacion/   Documentacion (contexto State), EstadoDocumentacion (abstracta),
│                        9 estados concretos, CambioEstadoDocumentacion, TipoDocumento,
│                        EstadoDocumentacionConverter
├── notificacion/  CU 29: InterfazNotificacionBolsin, NotificacionRecepcionBolsin (record con lo que se
│                  le notifica a UNA CM de origen) y NotificacionBolsinLog (redacta el correo en el log)
├── repositorio/   Spring Data JPA
├── dto/           records ...Request / ...Response
└── config/        bean Clock
back/src/main/resources/  application.properties, data.sql
back/src/test/resources/  fixtures aislados: datos-cmc.sql (A5), datos-varios-origenes.sql (CU 29)
front/src/                App.jsx (wizard de 5 pasos), services/api.js (una función por mensaje),
                          components/pasos/*.jsx (SeleccionDocumentacion.jsx para A3/A4/A5),
                          components/layout/MainLayout.jsx
docs/                     catedra/, diagramas/, sesiones/, diferencias_codigo_diagramas.md,
                          decisiones_para_la_tutora.md
(raíz)                    PENDIENTE.txt, COMO_EJECUTAR.txt, GUIA_DEMOSTRACION.md
```

### Cómo viaja un mensaje (igual que en el diagrama)

1. El front hace `POST /api/registro-recepcion-bolsin/<mensaje>`; el nombre es el del
   diagrama, por ejemplo `tomarSeleccionBolsin`.
2. La **Pantalla** recibe el mensaje y se lo pasa al **Gestor** con el mismo nombre.
3. El Gestor coordina a las entidades (`buscarRemitos()`, `actualizarBolsin()`, ...).
4. El Gestor le manda a la Pantalla los `mostrarX(...)` del diagrama. La Pantalla guarda lo que
   hay que mostrar y el endpoint se lo devuelve al front como JSON.

Detalles de esta forma de comunicación:

- **Referencia mutua.** La Pantalla y el Gestor se conocen entre sí, y Spring no puede crear
  dos beans que se necesitan en el constructor. Por eso el Gestor recibe la Pantalla con
  `@Lazy`. `back/lombok.config` hace que Lombok copie ese `@Lazy` al constructor.
- **Concurrencia.** La Pantalla y el Gestor guardan estado entre pedidos: es una pantalla para
  un EB. Por eso los 5 endpoints de la Pantalla son `synchronized`. Sin eso, dos pedidos
  simultáneos se mezclaban. Pasaba, por ejemplo, con React en modo desarrollo, que llama dos
  veces al abrir, y la lista de bolsines aparecía vacía. **No sacar el `synchronized`.**
  (Es una pantalla para un EB: dos navegadores a la vez comparten el mismo estado. Ver la
  decisión 14 en `docs/decisiones_para_la_tutora.md`.)

La tabla completa de los 68 mensajes del diagrama, cada uno con su línea de código, está en
`docs/sesiones/Sesion_01_implementacion_CU28.md`.

---

## 5. Patrón State (lo que se defiende)

- **Contexto: `Documentacion`.** Tiene `estadoActual` y el historial `cambioEstado`. Sus
  métodos `recibirYAceptar`, `recibirYRechazar`, `registrarNoRecibida` y `redirigir` sólo
  delegan, pasando también los datos del cambio:
  `estadoActual.recibirYAceptar(this, cambioEstado, fhActual, responsable)`.
- **Estado abstracto: `EstadoDocumentacion`** (clase abstracta).
  - Por defecto cada transición **lanza `IllegalStateException`**, que la API devuelve como
    409.
  - Tiene el código común `cambiarEstado(...)`, que:
    1. cierra el CambioEstado actual con `setFechaHoraFin`;
    2. llama a `crearCambioEstado(...)`;
    3. llama a `doc.setEstado(nuevo)` y a `doc.agregarCE(ce)`.

    Reusar ese código es lo que justifica la herencia.
- **Estados concretos:** Registrada, EnRemito, EnBolsinSaliente, EnBolsinEnviado,
  RecibidaYAceptada, RecibidaYRechazada, NoRecibida, ParaRedirigir y DeBaja. Cada uno redefine
  sólo las transiciones que salen de él:
  - `EnBolsinEnviado` puede ir a RecibidaYAceptada, RecibidaYRechazada, NoRecibida o
    ParaRedirigir;
  - `ParaRedirigir` puede ir a RecibidaYAceptada o RecibidaYRechazada;
  - `RecibidaYRechazada` puede ir a ParaRedirigir (`redirigir`), como en la máquina de estados.
- **Persistencia del estado:** se guarda el **nombre** del estado en una columna (por ejemplo
  `EnBolsinEnviado`). `EstadoDocumentacionConverter` (JPA `AttributeConverter`) recrea el
  objeto concreto al leer.
- **Quién elige la transición:** `DetalleRemito.actualizarDocumentacion(...)` (Experto), según lo
  que marcó el EB:

  | Flujo | Transición |
  |---|---|
  | Flujo descripto | `recibirYAceptar` |
  | A3 | `registrarNoRecibida` |
  | A4 | `recibirYRechazar` |
  | A5 | `redirigir` |

  No hay ningún `if` sobre nombres de estado.
- **Pendiente de decisión:** el diagrama de clases declara 9 operaciones más, de otros CU
  (`darDeBaja`, `enviar`, `incluirEnBolsin`, ...), que el código no tiene. Ver
  `docs/decisiones_para_la_tutora.md`, punto 10.

---

## 6. Datos de prueba (`data.sql`)

- **CMs:** 1 Central (CABA), 2 Córdoba, 3 Rosario, 4 Mendoza.
- **Empleados:** Laura Gómez (Córdoba), Martín Pérez (Rosario), Ana Ruiz (Central), Julián Sosa
  (Mendoza).
- **Sesión activa:** la que no tiene `fecha_hora_fin`. Hoy es la de Laura Gómez (usuario 1), así
  que **se trabaja como EB de Córdoba**.
- **Bolsines:**

  | Bolsín | Precinto | Origen → Destino | Estado | Remitos y documentación |
  |---|---|---|---|---|
  | 101 | 5001 | Central → Córdoba | Enviado | Remitos 1001 y 1002, documentación 1, 2 y 3 |
  | 102 | 5002 | Rosario → Córdoba | Enviado | Remito 1003 |
  | 103 | 5003 | Mendoza → Córdoba | Cerrado | No aparece en la lista |
  | 104 | 5004 | Central → Rosario | Enviado | No aparece: es de otra CM |
  | 105 | 5005 | Mendoza → Córdoba | Enviado | Remito 1006 |

- **Estados de remito:** RecibidoYAceptado (id 4) y RecibidoYAceptadoParcial (id 5).
- **Al reiniciar el back, los datos vuelven al estado inicial.** Para mostrar la persistencia
  entre reinicios, ver `GUIA_DEMOSTRACION.md`.
- **Fixtures aislados** (`back/src/test/resources`). No forman parte de la demo; los cargan las
  pruebas con `@Sql`:
  - `datos-cmc.sql` (A5): la EB pasa a ser Ana Ruiz (CMC) y aparece el bolsín **5006** (Córdoba →
    Central). La documentación 10 y 12 tienen área de la CMC; la 11, no. Para la demo se carga por
    argumento (Parte 2 de la guía).
  - `datos-varios-origenes.sql` (CU 29): agrega al 5001 un remito de Rosario con la documentación
    13.

---

## 7. Estado actual (qué funciona y está verificado)

Todo esto se verificó el 10/11 con JDK 21, en el front, en `/h2-console` y en el log.

| Flujo | Qué hace | Detalle |
|---|---|---|
| Flujo descripto (opción 1) | Toda la documentación pasa a RecibidaYAceptada, los remitos a RecibidoYAceptado y el bolsín a RecibidoEnCMDestino | Historial y responsable |
| A1 / A2 | Filtros sin resultados, en el front | Reverificados el 10/11 |
| A3 (opción 2) | La marcada queda NoRecibida, el resto RecibidaYAceptada; el remito al que le falta algo queda RecibidoYAceptadoParcial | Sesión 02 |
| A4 (opción 3) | La marcada queda RecibidaYRechazada, el resto RecibidaYAceptada; su remito queda parcial (*provisional*) | Sesión 03 |
| A5 (opción 4) | Sólo si el destino es la CMC. La marcada, con área registrada, queda ParaRedirigir y el resto RecibidaYAceptada; el remito queda RecibidoYAceptado (*provisional*) | Sesión 04; fixture `datos-cmc.sql` |
| A6 | No confirma: no se modifica ni se notifica nada | — |
| CU 29 | Una notificación por CM de origen, con el bolsín, la CM destino y cada documentación con su estado final. Si falla, no se registra la recepción | Sesión 05 |
| Robustez | Validación del orden de los pasos y de la selección (pertenencia al bolsín, área, CMC); errores 400/409; transacción con `saveAndFlush` antes de notificar | O1 de la sesión 06 |

Hay **25 pruebas** y todas pasan.

---

## 8. Diferencias justificadas entre el código y el diagrama

> **La lista completa y vigente** (flujo descripto, A3 a A5, CU 29, erratas del diagrama y lo que
> falta implementar) está en **`docs/diferencias_codigo_diagramas.md`**. Lo que sigue son las tres
> diferencias del flujo descripto, explicadas en detalle.

No hay que "corregirlas" en el código. Son decisiones tomadas porque el diagrama no se puede
cambiar y hay cosas que Java no permite hacer tal cual.

1. **`getNombre()` dentro de `obtenerCM()` y de `getOrigen()`.**
   - En el diagrama, Empleado (y Bolsín) le envía `getNombre()` a ComisionMedica dentro de
     `obtenerCM(): ComisionMedica` (y de `getOrigen(): ComisionMedica`).
   - En el código, esos métodos devuelven la `ComisionMedica`, como dice su firma, y el Gestor
     le pide `getNombre()` a esa CM.
   - Motivo: el Gestor necesita el **objeto** CM (para `esCmDestino(cm)`) y también su
     **nombre** (para `mostrarCM`). Un método no puede devolver las dos cosas. Si Empleado
     hiciera el `getNombre()`, tendría que devolver el texto y se rompería la firma. El Gestor
     ya conocía a ComisionMedica, así que no se agrega acoplamiento.
2. **`Bolsin.buscarRemitos()` devuelve `int[]`, no `remito[]`.**
   - Se respetó la flecha `*getNumero()` de Bolsín a Remito y lo que recibe la pantalla
     (`mostrarNumRemitosyDocumentacionAsociada(nrosRemito: int[], ...)`).
   - Si devolviera los objetos Remito, quien llamaría a `getNumero()` sería el Gestor (otra
     flecha), y el Gestor quedaría acoplado a Remito.
3. **`Bolsin.crearCambioEstado(fhInicio, e, responsable)` tiene parámetros; en el diagrama
   no.**
   - El `new(fhInicio, e, usuarioResponsable)` del diagrama necesita esos tres datos, y son
     parámetros de `actualizarEstado(...)`.
   - Sin parámetros, el bolsín tendría que guardar datos temporales como atributos, y eso es
     un mal diseño.
   - La versión de Documentación (`crearCambioEstado(fhInicio, nuevo, responsable)`) sí tiene
     parámetros en el diagrama, así que el código queda coherente con eso.

Otras diferencias menores:
- Para cerrar el CambioEstadoBolsin actual se busca con `sosActual()`, la misma búsqueda que el
  diagrama dibuja antes.
- `getOpcionesRecepcion()` devuelve `String[]`.
- El «include» del CU 29 va antes de `mostrarEjecucionExitosa()`, para no mostrar un éxito si
  la notificación falla.
- `saveAndFlush()`, `findAll()` y las validaciones son del diseño de persistencia y no aparecen
  en el diagrama.

Los errores de tipeo de los diagramas (por ejemplo `tomarConfirmaionRegistro`) se ignoran. El
código usa el nombre correcto.

---

## 9. Lo que falta (en orden)

La implementación del CU 28 y del CU 29 está terminada. El detalle está en `PENDIENTE.txt`.

1. **Decisiones para la tutora:** los 14 puntos de `docs/decisiones_para_la_tutora.md`. Los
   principales:
   - el estado del remito en A4 y A5;
   - el área obligatoria en A5;
   - la CMC reconocida por su nombre;
   - RNF 5 contra CU 28/29;
   - qué pasa si falla la notificación;
   - qué significa A6.
2. **Decisión del grupo:** declarar o no las 9 operaciones del State que pertenecen a otros CU, y
   unificar el criterio sobre los diagramas (AGENTS.md regla 1 contra la regla 1 de la sección 2).
3. **PDF de la Entrega 2,** sin apartado de UX. Lleva:
   - la carátula completa;
   - la identificación y justificación del patrón;
   - las vistas estática y dinámica;
   - los detalles de implementación;
   - los diagramas complementarios para A3, A4, A5 y el CU 29, armados a partir de
     `docs/diferencias_codigo_diagramas.md`.
4. **Integrar esta copia al repositorio del grupo.** El `.gitignore` deja afuera `back/target`,
   `back/data` y `front/node_modules`.
5. **Defensa:** todos tienen que poder seguir `GUIA_DEMOSTRACION.md` y explicar el State en el
   código (sección 5).
6. **Terminado:** se mejoró el frontend y se comprobó en escritorio y móvil emulado.

---

## 10. Para la IA: cómo trabajar en este proyecto

- Leé primero el diagrama correspondiente en `docs/diagramas/` (PDF) y la sección del CU en
  `docs/catedra/2026_DSI_Bolsines_ERS_V1.2.pdf`.
- **No renombres** métodos que ya coinciden con el diagrama. Si un método nuevo no está en el
  diagrama, decilo explícitamente y justificalo.
- Mantené el estilo del código existente:
  - nombres en español;
  - comentarios cortos;
  - records para los DTO;
  - Lombok `@RequiredArgsConstructor` y `@NoArgsConstructor(access = PROTECTED)` en las
    entidades;
  - sin getters/setters que nadie usa.
- Después de cada cambio:
  1. `.\mvnw.cmd clean install` (compila y corre las pruebas);
  2. levantar el back y el front;
  3. probar el flujo;
  4. mirar la base.
- Si un cambio agrega o modifica mensajes respecto del diagrama, registralo en
  `docs/diferencias_codigo_diagramas.md`. Las decisiones que no salen de la ERS van a
  `docs/decisiones_para_la_tutora.md`.
- Actualizá `PENDIENTE.txt` y agregá un archivo nuevo en `docs/sesiones/` con lo que se
  hizo en la sesión.
