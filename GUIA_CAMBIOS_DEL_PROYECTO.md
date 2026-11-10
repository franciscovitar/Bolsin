# Guía de cambios del CU 28 — Explicada para el grupo

**PPAI 2026 · Diseño de Sistemas de Información · 3K5 · Grupo 06**  
**Caso:** Bolsines · **CU 28:** Registrar recepción de bolsín  
**Versión explicada:** rama `cierre-cu28`, commit `86ef89e` (10/11/2026).  
**Comparación:** trabajo inicial del grupo en [PPAIDSIG6](https://github.com/juangedda/PPAIDSIG6), commit `d579662` (09/11/2026).

> **Para qué sirve esta guía:** que cualquiera del grupo, incluso quien no programó, entienda qué había antes, qué se agregó, por qué y dónde encontrarlo. **No reemplaza la ERS, los diagramas UML ni la documentación de la entrega.** Los tres diagramas de `docs/diagramas/` son los definitivos y se conservaron sin cambios.

## 1. Resumen en un minuto

El grupo ya había construido el proyecto con **Java 21, Spring Boot, React y H2**. Tenía implementado el **flujo normal** del CU 28: elegir un bolsín, controlar su contenido, indicar que llegó todo correctamente, confirmar y guardar la recepción. También contaba con los filtros **A1 y A2**, la cancelación **A6**, la estructura del patrón **State** y ocho pruebas. La notificación asociada al CU 29 avisaba en un log que se había recibido el bolsín, pero no detallaba la situación de cada documentación.

Sobre esa base se hicieron estas ampliaciones:

1. **A3:** registrar documentación que **no llegó**.
2. **A4:** registrar documentación que **no corresponde** a la comisión médica destino.
3. **A5:** marcar documentación que debe ir a **otra área de la Comisión Médica Central**.
4. **CU 29:** informar a **cada comisión médica de origen** el resultado de su propia documentación.
5. **Correcciones:** completar una transición State, reforzar validaciones y asegurar que se ejecuten los cambios SQL antes de notificar.
6. **Pruebas:** pasar de **8 a 25 pruebas automatizadas**.
7. **Trazabilidad UML:** comentarios en **27 clases Java** para relacionar código, diagrama de clases, secuencia y máquina de estados.
8. **Interfaz:** mejorar la apariencia institucional y adaptar las cinco pantallas a escritorio y móvil.

**Resultado reportado de la validación final:** 25 pruebas Java exitosas con JDK 21, build de React/Vite exitoso, recorridos normales y alternativos probados en Chrome de escritorio y móvil emulado, y consola limpia. Esta es una **versión candidata final del desarrollo**, no la entrega académica definitiva.

## 2. Qué pasa ahora con cada flujo

| Flujo | Situación | Qué hace el sistema | ¿Ya existía originalmente? |
|---|---|---|---|
| **Normal** | Llegó todo como estaba registrado | Documentación → `RecibidaYAceptada`; remitos → `RecibidoYAceptado`; bolsín → `RecibidoEnCMDestino` | **Sí** |
| **A1** | No se encuentra el número de precinto buscado | El filtro avisa que no hay coincidencia; no modifica datos | **Sí, en el front** |
| **A2** | No se encuentra la comisión médica origen buscada | El filtro avisa que no hay coincidencia; no modifica datos | **Sí, en el front** |
| **A3** | Falta una parte de la documentación | Lo marcado → `NoRecibida`; el resto → `RecibidaYAceptada`; remito afectado → `RecibidoYAceptadoParcial` | **Se agregó** |
| **A4** | Hay documentación que no corresponde al destino | Lo marcado → `RecibidaYRechazada`; el resto → `RecibidaYAceptada`; remito afectado → `RecibidoYAceptadoParcial` | **Se agregó** |
| **A5** | Hay que redirigir documentación dentro de la Central | Lo marcado → `ParaRedirigir`; el resto → `RecibidaYAceptada`; remitos → `RecibidoYAceptado` | **Se agregó** |
| **A6** | El encargado no confirma | No guarda cambios ni envía notificaciones | **Sí** |

**Ejemplo concreto de A3:** al recibir el bolsín **5001**, el encargado marca que la documentación **2** no llegó. Al confirmar, esa documentación queda `NoRecibida`, las documentaciones 1 y 3 quedan `RecibidaYAceptada`, el remito 1001 queda parcialmente aceptado y el 1002 aceptado. El sistema guarda responsable, fecha y hora.

**Importante:** los estados finales del remito en A4 y A5, y algunos casos límite, son **decisiones provisionales del grupo** porque la ERS no los define de manera explícita. Ver `docs/decisiones_para_la_tutora.md`.

## 3. ¿Dónde está cada parte en el código y en los diagramas?

El recorrido conceptual es:

`Encargado de Bolsines → Pantalla → Gestor → Bolsín → Remito → DetalleRemito → Documentación → Estado concreto`

| En el diseño UML | Archivo principal del código | Para entenderlo fácilmente |
|---|---|---|
| **Pantalla / «boundary»** | `back/src/main/java/ppai/bolsines/pantalla/PantallaRegistroRecepcionBolsines.java` | Recibe los mensajes del actor mediante la API REST y devuelve respuestas a React |
| **Gestor / «control»** | `.../gestor/GestorRegistroRecepcionBolsines.java` | Coordina el caso de uso; recuerda el bolsín, la opción y la documentación marcada |
| **Bolsín** | `.../modelo/bolsin/Bolsin.java` | Conoce sus remitos, actualiza su estado y su historial |
| **Remito** | `.../modelo/remito/Remito.java` | Conoce sus detalles y determina si queda aceptado o parcialmente aceptado |
| **DetalleRemito** | `.../modelo/remito/DetalleRemito.java` | Sabe qué documentación contiene y qué transición debe pedirle |
| **Documentación — CONTEXT** | `.../modelo/documentacion/Documentacion.java` | Recibe `recibirYAceptar`, `recibirYRechazar`, `registrarNoRecibida` o `redirigir` y **delega** al estado actual |
| **EstadoDocumentacion — STATE abstracto** | `.../modelo/documentacion/EstadoDocumentacion.java` | Define esas operaciones; rechaza las no permitidas y comparte `cambiarEstado()` |
| **Estados concretos — CONCRETE STATE** | `.../modelo/documentacion/EnBolsinEnviado.java` y demás clases del mismo paquete | Cada estado determina qué transiciones permite y cuál es el siguiente estado |
| **Historial** | `.../modelo/documentacion/CambioEstadoDocumentacion.java` | Conserva inicio, fin, estado y empleado responsable |
| **«include» CU 29** | `.../notificacion/InterfazNotificacionBolsin.java` y `NotificacionBolsinLog.java` | Construye y registra un aviso por CM de origen; **no envía emails reales** |

En la tabla, `.../` significa `back/src/main/java/ppai/bolsines/`.

**Para seguir una llamada del diagrama de secuencia:** buscá, por ejemplo, `tomarConfirmacionRegistro` en la Pantalla y luego en el Gestor. El Gestor invoca `actualizarBolsin` y se recorre la cadena de entidades hasta llegar al estado concreto.

**Para seguir una flecha de la máquina de estados:** buscá su método en `EnBolsinEnviado.java`. Por ejemplo, `registrarNoRecibida` termina llamando al método común `cambiarEstado(..., new NoRecibida())`. No se decide el estado siguiente comparando cadenas de texto; se usa polimorfismo.

Los comentarios agregados en las 27 clases citan expresamente mensajes, transiciones, roles de State y diferencias de firmas con los UML.

### Los tres diagramas definitivos

- `docs/diagramas/DIAGRAMA CLASES.pdf`: clases, métodos, asociaciones y roles.
- `docs/diagramas/SECUENCIA PATRON.pdf`: mensajes y responsabilidades del **flujo descripto**, especialmente la actualización con State.
- `docs/diagramas/MaquinaEstadosPPAI (1).pdf`: estados y transiciones de Documentación.

**No se editaron esos diagramas.** A3, A4, A5 y el detalle de CU 29 requieren mensajes y parámetros adicionales que no aparecen en la secuencia del flujo descripto. Eso **no debe presentarse como una coincidencia exacta**. Todas las diferencias están explicadas en `docs/diferencias_codigo_diagramas.md`.

## 4. Cambios explicados por tema

### A. Selección de documentación para A3, A4 y A5

**Antes:** se elegía la opción de recepción y se podía confirmar sin indicar documentos específicos.

**Ahora:** si se elige A3, A4 o A5, aparece un paso donde se marca **exactamente qué documentación** presenta la situación elegida. Se usa su número identificador, no su asunto (dos documentos podrían tener el mismo asunto).

**Dónde verlo:** `front/src/components/pasos/SeleccionDocumentacion.jsx`, `front/src/App.jsx`, los DTO del backend y `GestorRegistroRecepcionBolsines.tomarSeleccionDocumentacion`.

**Por qué está bien distribuido:** el Gestor valida qué eligió el actor, pero la decisión de transición se delega hacia `DetalleRemito` y finalmente hacia `Documentacion` y su estado concreto. Así se respetan **Experto, Controlador y State**.

### B. Transición que faltaba en State

Se agregó `RecibidaYRechazada.redirigir()` para permitir el paso **RecibidaYRechazada → ParaRedirigir**, ya dibujado en la máquina de estados. También se probó el cierre del estado anterior y la creación del nuevo cambio con el empleado correcto.

**Dónde verlo:** `modelo/documentacion/RecibidaYRechazada.java` y `EstadoDocumentacionTest.java`.

### C. Notificación del CU 29 más completa

**Antes:** se registraba en el log un aviso general de que había llegado un bolsín.

**Ahora:** se prepara una notificación **por cada CM de origen de los remitos** y se incluye sólo su documentación, con número, asunto y estado final. Se probaron dos orígenes distintos sin mezclar sus documentos.

**Dónde verlo:** `GestorRegistroRecepcionBolsines.llamarCasoUso29()`, `modelo/bolsin/Bolsin.java`, `modelo/remito/Remito.java`, `notificacion/NotificacionRecepcionBolsin.java` y `NotificacionBolsinLog.java`.

**No confundir:** la notificación sigue siendo **simulada en el log**. No se integró un servicio real de correo.

### D. Persistencia y correcciones de seguridad funcional

- Se utiliza `saveAndFlush` antes de la notificación para forzar el SQL antes del aviso. **No garantiza** que un eventual fallo posterior del commit pueda deshacer una notificación ya emitida.
- Se valida que la documentación marcada pertenezca al bolsín, que no quede una selección vacía y, en A5, que el destino sea la **CMC** y que el documento a redirigir tenga área registrada.
- Se corrigió un error por el cual, al seleccionar **otro bolsín** por la API, podían reutilizarse la opción y las marcas del anterior. Ahora se invalidan; una prueba comprueba el rechazo, la ausencia de cambios y la posibilidad de continuar correctamente.

**Dónde verlo:** `GestorRegistroRecepcionBolsines.java` y `RegistroRecepcionBolsinIntegracionTest.java`.

### E. Pruebas más completas

**Antes:** ocho pruebas del patrón State.

**Ahora:** 25 pruebas automatizadas de State, DTO, notificaciones e integración con H2 en memoria. Cubren los flujos principales y alternativos, sus estados e historiales, cancelación, destinatarios, validaciones y correcciones.

Las pruebas **no sustituyen** la demostración en la interfaz: también se completó una verificación en Chrome de escritorio (1440×900) y móvil emulado (390×844), con los flujos normal, A3, A4, A5 y A6. El build de Vite terminó bien; su aviso por tamaño del bundle no impidió compilar.

### F. Mejora visual

**Antes:** interfaz de cinco pasos con un tema básico/provisorio.

**Ahora:** estilo institucional azul, encabezados consistentes, mejor lectura de remitos y documentación, tarjetas para elegir opciones, selección resaltada, confirmación más ordenada y adaptación a celular.

**Dónde verlo:** `front/src/theme.js`, `front/src/components/layout/MainLayout.jsx`, `front/src/components/comunes/EncabezadoPaso.jsx`, `DatosDocumento.jsx` y los componentes de `front/src/components/pasos/`.

No se cambió la lógica del caso de uso ni los contratos de API. **El comunicado docente excluyó el apartado formal de UX para la Entrega 2**: esta fue una mejora visual, no una entrega de moodboard, mapa de empatía o user journey.

## 5. ¿Cómo mostrarlo sin perderse?

Con **JDK 21** y Node.js compatibles, abrir dos terminales desde la raíz del proyecto:

```powershell
# Terminal 1
cd back
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

```powershell
# Terminal 2
cd front
npm ci
npm run dev
```

Abrir `http://localhost:3000`. La demo normal usa **Laura Gómez / Comisión Médica Córdoba** y los precintos **5001, 5002 y 5005**. Para probar A3, por ejemplo, elegir 5001, opción 2, marcar la documentación 2 y confirmar. Para no modificar la demo durante un ensayo, elegir **No confirmar**.

**A5 requiere datos específicos de la Central**, que están aislados en `back/src/test/resources/datos-cmc.sql`. La forma exacta de iniciar ese escenario y volver a la demo normal está explicada en `GUIA_DEMOSTRACION.md` y `COMO_EJECUTAR.txt`.

La base H2 normal se recrea con los datos iniciales al arrancar el backend; para comprobar persistencia entre reinicios hay una variante documentada en `GUIA_DEMOSTRACION.md`. Eso es una configuración de la demo, no una limitación inherente de H2.

## 6. Qué quedó igual y qué NO se hizo

- **Los tres diagramas UML definitivos se mantienen intactos.**
- **Sólo se implementó el CU 28**, incluida la integración con CU 29 necesaria para ese flujo. No se implementaron otros casos de uso.
- El patrón State se aplica a **Documentación**, no a las entidades catálogo `EstadoBolsin` y `EstadoRemito`.
- Las otras operaciones de State que aparecen en el diagrama para casos de uso diferentes **no se implementaron**. La diferencia está documentada.
- No se envían emails reales y no se implementó autenticación nueva: se usa la sesión precargada de la demo.
- La pantalla y el gestor están concebidos para un encargado usando una instancia a la vez; **no es una solución multiusuario de producción**.
- No se creó el PDF de entrega ni se alteraron los artefactos de la cátedra.

## 7. Decisiones para conversar con la tutora o el grupo

El proyecto funciona para los escenarios probados, pero siguen siendo **decisiones de diseño pendientes de validación**, no requisitos explícitos de la ERS:

- Estado final de remitos en **A4** y **A5**, incluido el caso de un remito con todo marcado.
- Exigir un área registrada en A5 y detectar la Central por su **nombre**.
- A quién notificar: los **CU 28/29 indican CM de origen**, mientras que el **RNF 5 menciona CM destino**. El código sigue a los casos de uso.
- Reglas sobre confirmación/cancelación, transacciones, sesiones y operaciones State de otros CU.
- Cómo explicar las diferencias entre diagramas definitivos y los métodos adicionales del código.

La lista completa, con contexto y alternativas, está en **`docs/decisiones_para_la_tutora.md`**.

## 8. Dónde mirar si alguien necesita más detalle

| Pregunta | Archivo recomendado |
|---|---|
| ¿Cómo ejecutamos el sistema? | `COMO_EJECUTAR.txt` |
| ¿Cómo hacemos una demostración del CU y de A5? | `GUIA_DEMOSTRACION.md` |
| ¿Qué cambió respecto del UML? | `docs/diferencias_codigo_diagramas.md` |
| ¿Por qué se eligieron ciertos estados o reglas? | `docs/decisiones_para_la_tutora.md` |
| ¿Qué se hizo en cada etapa de programación? | `docs/sesiones/Sesion_01…Sesion_06` |
| ¿Qué quedó pendiente del grupo? | `PENDIENTE.txt` |
| ¿Cómo se relaciona cada método Java con el UML? | Comentarios dentro de `back/src/main/java/ppai/bolsines/` |

---

**Conclusión:** el grupo construyó la base funcional y el modelado. Después se completaron A3–A5, se amplió la notificación, se reforzaron las validaciones y las pruebas, se añadieron comentarios de trazabilidad y se mejoró la presentación. El desarrollo quedó **validado como candidato final**, sujeto a las decisiones académicas pendientes y a la preparación de la entrega por parte del grupo.
