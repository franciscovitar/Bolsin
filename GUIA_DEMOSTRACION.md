# Guía de demostración: CU 28 Registrar recepción de bolsín (+ CU 29)

PPAI DSI 2026 · 3K5 · Grupo 06 · Actualizado al 10/11/2026

Esta guía muestra cómo probar el flujo descripto, los seis alternativos, la persistencia y el
patrón State. Cómo instalar y levantar el proyecto está en `COMO_EJECUTAR.txt`.

**Antes de empezar:** usar una terminal con **JDK 21**. Si la PC tiene varios JDK:
`$env:JAVA_HOME = "C:\ruta\al\jdk-21"`.

---

## Parte 1: demo normal (EB de Córdoba)

```powershell
# Terminal 1
cd back
.\mvnw.cmd spring-boot:run
# Terminal 2
cd front
npm run dev
```

Abrir http://localhost:3000. Cada arranque del back vuelve a cargar los datos de `data.sql`.
Para repetir la demo desde cero, alcanza con reiniciar el back.

El orden de abajo usa cada bolsín una sola vez.

| # | Qué se hace | Qué tiene que pasar |
|---|---|---|
| 1 | Abrir la pantalla | Arriba a la derecha dice "Comisión Médica Córdoba". Aparecen los bolsines **5001** (Central), **5002** (Rosario) y **5005** (Mendoza) |
| 2 | **A1:** escribir `9999` en "Número de precinto" | "No se encontró un bolsín con el número de precinto "9999"." Borrar el filtro |
| 3 | **A2:** escribir `Salta` en "Comisión médica origen" | "No se encontró un bolsín de la comisión médica origen "Salta"." Borrar el filtro (con `mendoza` aparece el 5005) |
| 4 | **A6:** con el 5002 → Continuar → opción 1 → **No confirmar** | "Registro cancelado: el bolsín no se modificó." El 5002 sigue en la lista |
| 5 | **Flujo descripto:** con el 5002 → opción 1 → **Confirmar recepción** | Mensaje de éxito. En la terminal del back aparece el correo del CU 29 a `cm.rosario@bolsines.test` con la documentación 4 y 5 en RecibidaYAceptada |
| 6 | **A5 rechazado:** con el 5001 → opción 4 ("redirigir a otra área") | "...sólo corresponde cuando la comisión médica destino es la Central". El EB queda en la lista de opciones |
| 7 | **A3:** en el mismo 5001 → opción 2 → marcar "Dictamen incapacidad - Gómez, Ana" → Continuar → Confirmar | Éxito. El correo a `cm.central@bolsines.test` muestra la documentación 2 en **NoRecibida** y la 1 y la 3 en RecibidaYAceptada |
| 8 | **A4:** con el 5005 → opción 3 → marcar "Historia clínica - Paz, Eva" → Confirmar | Éxito. El correo a `cm.mendoza@bolsines.test` muestra la documentación 8 en **RecibidaYRechazada** y la 9 en RecibidaYAceptada |
| 9 | "Registrar otra recepción" | "No hay bolsines enviados a su comisión médica." |

En los pasos 7 y 8 se puede tocar "Elegir otra opción" para volver a las opciones, y "Cancelar
registro" en cualquier paso (Obs. 1 del CU).

### Base de datos: mostrar la persistencia y el historial

http://localhost:8080/h2-console. JDBC URL `jdbc:h2:file:./data/bolsines`, usuario `sa`,
contraseña vacía.

```sql
-- Estado de cada documentación y de su remito
SELECT d.numero, d.estado_actual, r.numero AS remito, er.nombre AS estado_remito, r.bolsin_numero
FROM documentacion d
JOIN detalle_remito dr ON dr.documentacion_numero = d.numero
JOIN remito r ON r.numero = dr.remito_numero
JOIN estado_remito er ON er.id = r.estado_id
ORDER BY d.numero;

-- Historial de la documentación: el anterior se cierra en el mismo instante en que empieza el nuevo, con el responsable
SELECT documentacion_numero, estado, fecha_hora_inicio, fecha_hora_fin, responsable_id
FROM cambio_estado_documentacion WHERE documentacion_numero IN (1,2,3,4,5,8,9)
ORDER BY documentacion_numero, fecha_hora_inicio;

-- Historial del bolsín
SELECT c.bolsin_numero, e.nombre, c.fecha_hora_inicio, c.fecha_hora_fin, c.responsable_id
FROM cambio_estado_bolsin c JOIN estado_bolsin e ON e.id = c.estado_id
WHERE c.bolsin_numero IN (101,102,105) ORDER BY c.bolsin_numero, c.fecha_hora_inicio;
```

Lo esperado después de la Parte 1:

| Elemento | Resultado |
|---|---|
| Documentación | 1, 3, 4, 5 y 9 en RecibidaYAceptada; 2 en NoRecibida; 8 en RecibidaYRechazada |
| Remitos | 1001 y 1006 en **RecibidoYAceptadoParcial**; 1002 y 1003 en RecibidoYAceptado |
| Bolsines | 101, 102 y 105 en RecibidoEnCMDestino, con responsable 1 (Laura) |

### (Opcional) La persistencia sobrevive a un reinicio

Por defecto la base se recrea en cada arranque. Para mostrar que lo guardado persiste, cortar el
back (Ctrl + C) y levantarlo **sin recrear la base**:

```powershell
cd back
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.jpa.hibernate.ddl-auto=update --spring.sql.init.mode=never"
```

Los bolsines recibidos ya no aparecen en la lista. Para volver a la demo desde cero, levantar el
back normal.

---

## Parte 2: A5 en la Comisión Médica Central (fixture de prueba)

`data.sql` no tiene bolsines enviados a la Central. A5 se muestra con el fixture
`back/src/test/resources/datos-cmc.sql`, que se carga **sólo por argumento** y no modifica la
demo:
- la EB logueada pasa a ser Ana Ruiz (CMC);
- aparece el bolsín **5006** (Córdoba → Central), con el remito 1007 (documentación 10, "Área de
  Dictámenes", y 11, sin área) y el remito 1008 (documentación 12, "Mesa de Entradas").

```powershell
# Cortar el back (Ctrl + C) y levantarlo con el fixture:
cd back
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.sql.init.data-locations=classpath:data.sql,file:src/test/resources/datos-cmc.sql"
```

| # | Qué se hace | Qué tiene que pasar |
|---|---|---|
| 1 | Recargar el front | Arriba dice "Comisión Médica Central". Sólo aparece el **5006** |
| 2 | Seleccionar el 5006 | Se ve el área de la CMC de cada documentación ("Área: Área de Dictámenes", "Área: Mesa de Entradas") |
| 3 | Opción 4 → marcar "Dictamen apelación - Mena, Raúl" (sin área) → Continuar | "La documentación 11 no tiene registrada el área de la Comisión Médica Central a la que va" |
| 4 | Marcar "Expediente 101/2026 - Sosa, Lía" → Continuar → Confirmar | Éxito. El correo a `cm.cordoba@bolsines.test` muestra la documentación 10 en **ParaRedirigir** y la 11 y la 12 en RecibidaYAceptada |
| 5 | En `/h2-console`, con la consulta de arriba y `r.bolsin_numero = 106` | 10 en ParaRedirigir; 11 y 12 en RecibidaYAceptada; remitos 1007 y 1008 en RecibidoYAceptado; responsable 3 (Ana) |

Para volver a la demo normal: cortar el back y levantarlo con `.\mvnw.cmd spring-boot:run`.

---

## Parte 3: el patrón State en el código (recorrido sugerido)

1. **Contexto:** `back/src/main/java/ppai/bolsines/modelo/documentacion/Documentacion.java`.
   `recibirYAceptar`, `recibirYRechazar`, `registrarNoRecibida` y `redirigir` sólo delegan:
   `estadoActual.x(this, cambioEstado, fhActual, responsable)`.
2. **Estado abstracto:** `EstadoDocumentacion.java`. Cada transición rechaza por defecto
   (`IllegalStateException`, que llega como 409). `cambiarEstado(...)` es el código común:
   cierra el cambio de estado actual, crea el nuevo con el responsable y se lo asigna a la
   documentación.
3. **Estados concretos:** `EnBolsinEnviado.java` redefine las 4 transiciones del CU 28;
   `ParaRedirigir.java` y `RecibidaYRechazada.java` redefinen las suyas. Los demás no redefinen
   nada.
4. **Quién elige la transición (Experto):** `DetalleRemito.actualizarDocumentacion(...)` según
   lo que marcó el EB. No hay ningún `if` sobre nombres de estado.
5. **Persistencia del estado:** `EstadoDocumentacionConverter.java` guarda el nombre y recrea el
   objeto concreto al leer.
6. **Pruebas:**
   - `EstadoDocumentacionTest`: State, sin base de datos;
   - `RegistroRecepcionBolsinIntegracionTest`: el CU completo contra una H2 en memoria, incluido
     el contenido del CU 29.

   Correrlas con `.\mvnw.cmd clean install`: **25 pruebas**.

Material de apoyo:
- `docs/sesiones/Sesion_01` a `Sesion_06`;
- `docs/diferencias_codigo_diagramas.md`;
- `docs/decisiones_para_la_tutora.md`.
