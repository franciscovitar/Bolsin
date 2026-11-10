# Sesión 04: flujo alternativo A5 "La documentación se debe redirigir a otra área"

PPAI DSI 2026 · 3K5 · Grupo 06 · Fecha: 10/11/2026

Con A5 quedan implementados todos los flujos alternativos del CU 28: A1/A2 (filtros), A3, A4, A5 y
A6. Los diagramas originales (`docs/diagramas/`) **no se modificaron**. Este documento registra la
extensión para A5. La de A3 está en la sesión 02 y la de A4 en la sesión 03.

## 1. Regla de negocio (ERS v1.2, "Recepción de un Bolsín en una CM Destino")

> Documentación para redirigir a otra área: ocurre cuando la CM destino es la **CMC**, que recibe
> documentación que debe ser enviada a un área dentro de la sede central. En este caso la
> documentación se actualiza al estado **Para redirigir**.
> [...] el envío a las áreas correspondientes no se realiza a través de un Bolsín, sino que se
> realiza de manera interna. Cuando el área [...] recibe la documentación verifica si es correcta
> [...] recibida y aceptada [...] recibida y rechazada.

| Elemento | Resultado en A5 | Fuente |
|---|---|---|
| Condición | Sólo si la CM destino del bolsín es la CMC; si no, la opción se rechaza (409) | ERS + guarda de la máquina de estados (`[la CM es destino es CMC y se debe redirigir a otra area]/redirigir()`) |
| Documentación marcada por el EB | EnBolsinEnviado → **ParaRedirigir** (`redirigir`) | ERS + máquina de estados |
| Resto de la documentación | EnBolsinEnviado → **RecibidaYAceptada** | ERS |
| Bolsín | **RecibidoEnCMDestino** | ERS |
| Remitos | **RecibidoYAceptado** (la documentación redirigida no deja el remito parcial) | **Decisión provisional del grupo**, que hay que validar con la tutora. La ERS **no** lo establece |
| CU 29 | Se invoca igual que en los demás flujos | Paso 12 del CU |
| Entrega interna al área (ParaRedirigir → RecibidaYAceptada / RecibidaYRechazada) | **No se implementa**: pertenece a otro alcance. Las transiciones ya existen en el State y tienen pruebas unitarias | ERS |

### Por qué el remito queda RecibidoYAceptado (decisión provisional)

- La documentación que se redirige **sí corresponde** a la CM destino, que es la CMC, y **llegó**.
  Es distinto de A3 (no llegó) y de A4 (no corresponde), donde el remito queda parcial.
- La redirección es un movimiento **interno** de la CMC, posterior a la recepción. Que el área la
  acepte o la rechace se decide después y no es parte de recibir el remito.
- Los únicos estados de remito para la recepción son "recibido y aceptado" y "recibido y aceptado
  parcial". Usar "parcial" sugeriría que faltó o se rechazó algo, y no es así.

## 2. Cómo se reconoce la CMC y para qué sirve `areaCMCDestino`

- **CMC.** En el modelo de dominio, `ComisionMedica` sólo tiene código, dirección, email, nombre y
  teléfono: ningún atributo dice si es la Central. El glosario de la ERS define "CMC: Comisión
  médica central" como una sola CM. Por eso:
  - `ComisionMedica.esCentral()` compara su nombre con "Comisión Médica Central", con el mismo
    criterio que `EstadoBolsin.sosEnviado()`;
  - el bolsín responde `esDestinoCentral()` preguntándole a su destino (Experto).

  No se agregó ningún atributo.
- **`DetalleRemito.areaCMCDestino`** es el único dato del modelo que dice a qué área de la CMC va
  cada documentación:
  - se muestra al EB al controlar el contenido y al marcar;
  - viaja en la fila `[nroRemito, nroDocumentacion, asunto, tipo, areaCMCDestino]` y en
    `DatosDocumentacion.areaCMCDestino`;
  - sólo se puede redirigir documentación que tenga un área registrada: si no la tiene, se responde
    400 y no se guarda nada.

  No se inventan áreas ni se le pide al EB que escriba una. **Esta validación también es una
  decisión del grupo para consultar con la tutora.**

## 3. Mensajes nuevos o modificados (extensión de la secuencia)

```
EB      -> Pantalla : tomarSeleccionOpcionRecepcion("La documentación se debe redirigir a otra área")
Pantalla-> Gestor   : tomarSeleccionOpcionRecepcion(opcion)
Gestor  -> selec:Bolsin : esDestinoCentral(): boolean                              [nuevo]
Bolsin  -> ComisionMedica (destino) : esCentral(): boolean                         [nuevo]
   [no es la CMC] Gestor -> error 409 "sólo corresponde cuando la comisión médica destino es la Central"
Gestor  -> Pantalla : solicitarSeleccionDocumentacionParaRedirigir()               [nuevo]
EB      -> Pantalla : tomarSeleccionDocumentacion(nrosDocumentacion: int[])        [el de A3/A4]
Pantalla-> Gestor   : tomarSeleccionDocumentacion(nrosDocumentacion: int[])        (valida pertenencia y área)
Gestor  -> Pantalla : solicitarConfirmacionRegistro()
EB      -> Pantalla : tomarConfirmacionRegistro(confirmacion)
Gestor  -> selec:Bolsin : actualizarEstado(..., nrosNoRecibida, nrosRechazada, nrosParaRedirigir)   [+1 parámetro]
Bolsin  -> Remito       : actualizarEstado(..., nrosNoRecibida, nrosRechazada, nrosParaRedirigir)   [+1 parámetro]
Remito  -> DetalleRemito: *esNoAceptada(nrosNoRecibida, nrosRechazada): boolean    [antes esMarcada]
Remito  -> DetalleRemito: actualizarDocumentacion(fhActual, responsable, nrosNoRecibida, nrosRechazada, nrosParaRedirigir)
DetalleRemito -> Documentacion: redirigir(fhActual, responsable)                   (A5: marcada)
Documentacion -> EnBolsinEnviado: redirigir(this, historial, fhActual, responsable)
EnBolsinEnviado -> new ParaRedirigir + cambiarEstado(...)                          (ya estaba en el State)
Gestor  -> CU 29 «include»                                                         (sin cambios)
```

## 4. Diferencias con los diagramas (sólo lo nuevo de A5)

| Diagrama | Código |
|---|---|
| Clases: ComisiónMédica | + `esCentral(): boolean` |
| Clases: Bolsín | + `esDestinoCentral(): boolean`; `actualizarEstado(...)` + `nrosParaRedirigir`; `buscarDocumentacion()` devuelve también el área |
| Clases: Remito | `actualizarEstado(...)` + `nrosParaRedirigir`; `tieneDocumentacionMarcada` pasó a llamarse `tieneDocumentacionNoAceptada` |
| Clases: DetallesRemito | `actualizarDocumentacion(...)` + `nrosParaRedirigir`; `esMarcada` pasó a llamarse `esNoAceptada`; `buscarDatosDocumentacion()` devuelve `areaCMCDestino` |
| Clases: Gestor | + `nrosDocumentacionParaRedirigir: int[]`; la opción 4 deja de responder "no implementada" |
| Clases: Pantalla | + `solicitarSeleccionDocumentacionParaRedirigir()` |
| Máquina de estados / State | **Sin cambios**: `EnBolsinEnviado.redirigir` ya existía y tenía prueba |

La tabla acumulada de A3 y A4 está en la sección 3 de la sesión 03.

## 5. Correcciones encontradas durante esta sesión

- **`tomarSeleccionDocumentacion` sin opción elegida.** Si se llamaba antes de elegir una opción,
  `List.of(...).contains(null)` lanzaba `NullPointerException`, que llegaba al front como 500. Ahora
  responde 409 ("La opción de recepción elegida no pide seleccionar documentación"). El error venía
  de A3/A4 y lo detectó la prueba nueva `a5ConDestinoDistintoDeCmcNoSePuedeElegir`.

## 6. Datos de prueba aislados (no se tocó la demo)

`back/src/test/resources/datos-cmc.sql` es un **fixture de prueba**. No es parte de `data.sql`:

- cierra la sesión de Laura y abre la de **Ana Ruiz (EB de la CMC)**;
- agrega el bolsín **106**, con precinto **5006**, de Córdoba a la Central, en estado Enviado, con:
  - el remito **1007**: documentación 10 ("Área de Dictámenes") y 11 (sin área);
  - el remito **1008**: documentación 12 ("Mesa de Entradas").

Formas de usarlo:
- **En las pruebas:** `@Sql("/datos-cmc.sql")` en los métodos de A5, sobre la H2 en memoria.
- **Para mostrar A5 en el front** (sin cambiar ninguna configuración):
  ```powershell
  cd back
  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.sql.init.data-locations=classpath:data.sql,file:src/test/resources/datos-cmc.sql"
  ```
  Al volver a arrancar con `.\mvnw.cmd spring-boot:run`, la demo es la de siempre: Laura,
  Córdoba, con el 5001, el 5002 y el 5005.

## 7. Pruebas

| Prueba | Qué verifica |
|---|---|
| `a5ConDestinoCmcLaDocumentacionMarcadaQuedaParaRedirigir` | Ana ve sólo el 5006 y el área de la documentación:<br>• la 10 queda ParaRedirigir; la 11 (mismo remito) y la 12, RecibidaYAceptada;<br>• los remitos 1007 y 1008 quedan RecibidoYAceptado;<br>• el bolsín 106 queda RecibidoEnCMDestino;<br>• el historial queda bien, con responsable Ana (3);<br>• se invoca el CU 29 (`cm.cordoba`, 5006) |
| `a5ConDestinoDistintoDeCmcNoSePuedeElegir` | Laura con el 5001:<br>• la opción 4 da 409 y no queda elegida;<br>• no se puede marcar ni confirmar;<br>• nada cambia y no se invoca el CU 29 |
| `a5ValidaLaSeleccionSinGuardarNadaAMedias` | Se rechazan:<br>• una documentación sin área;<br>• una de otro bolsín;<br>• `[10, 11]` (no se guarda ninguna);<br>• una selección vacía.<br>Confirmar sigue pidiendo la selección |
| `a5DespuesDeMarcarNoConfirmaYNadaCambia` | A6 después de marcar 10 y 12: nada cambia, el historial no crece y no se invoca el CU 29 |
| Regresión | Flujo descripto, A3, A4 (2), A6 y validaciones de A3. Además, `EstadoDocumentacionTest` (9) y `SeleccionBolsinResponseTest` (1), que ahora incluye el área |

Resultado con JDK 21 (`mvnw -o clean install`): **20 pruebas, 0 fallas**.

Prueba manual del 10/11:
- **Back con el fixture:**
  - A5 en el front con el 5006: marcar la 11 (sin área) muestra el error; marcar la 10 y confirmar
    funciona;
  - en `/h2-console`, la 10 quedó ParaRedirigir, la 11 y la 12 RecibidaYAceptada, los remitos 1007
    y 1008 RecibidoYAceptado y el 106 RecibidoEnCMDestino, todo con responsable 3 y en el mismo
    instante;
  - en el log: `CU 29 - Correo a cm.cordoba@bolsines.test ... 5006 ...`.
- **Back con la demo normal:**
  - en el front, Laura con el 5001 y la opción 4 ve el mensaje "sólo corresponde cuando la comisión
    médica destino es la Central";
  - por la API (proxy del front): A6 y A4 con el 5005, A3 con el 5001 y el flujo descripto con el
    5002. Al final no quedan bolsines enviados a Córdoba.

## 8. Pendiente e inconsistencias

- **Validar con la tutora:**
  - el remito RecibidoYAceptado en A5;
  - la exigencia de un área registrada para redirigir;
  - el remito parcial en A4.
- **CMC reconocida por nombre:** si la cátedra prefiere otro criterio, el cambio queda dentro de
  `ComisionMedica.esCentral()`.
- **Parámetros largos:** `Bolsin.actualizarEstado` tiene 8 parámetros, porque los tres arreglos de
  documentación marcada viajan juntos por Bolsín, Remito y DetalleRemito. Se podrían agrupar en un
  objeto, pero eso agrega una clase que no está en el modelo de dominio. Queda para decidir.
- **Comparación del texto de la opción en el gestor:** hay dos cadenas `if` sobre la opción (pedir
  la selección y guardarla). La alternativa es modelar cada opción como un objeto.
- **La opción 4 se ofrece siempre** (`getOpcionesRecepcion()` del diagrama devuelve las 4) y se
  rechaza al elegirla si el destino no es la CMC. Otra posibilidad es ocultarla.
- **CU 29:** no informa la situación de la documentación (no recibida, rechazada o para redirigir).
- **Documentación:** `PENDIENTE.txt`, `COMO_EJECUTAR.txt` y `CONTEXTO_IA.md` siguen diciendo que
  las opciones 2 a 4 no están implementadas.
