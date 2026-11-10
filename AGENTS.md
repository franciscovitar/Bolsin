# Reglas del proyecto (para el equipo y para asistentes de IA)

El código se corrige por **consistencia con el modelado** y se defiende oralmente. Estas reglas
salen de las consignas, de los apuntes de la cátedra y de los ejemplos que dio la cátedra
(Consultorio con State, repositorio "Registrar Laboreo en Lotes").

## Consistencia con los diagramas
1. Las clases, atributos y métodos se llaman igual que en los diagramas de diseño (clases y
   secuencia). Si al implementar hace falta cambiar algo, primero se corrige el diagrama.
2. Pantalla = `@RestController` (boundary). Un endpoint por cada mensaje del actor a la pantalla,
   con el nombre del mensaje. Los `mostrarX()` del gestor son métodos de la pantalla que guardan lo
   que hay que mostrar; el endpoint lo devuelve como respuesta (records en `dto`).
3. Gestor = `@Service` (control). Coordina y recuerda las selecciones del actor; no resuelve reglas
   que puede resolver una entidad.
4. Experto: preguntar a quien conoce (`documentacion.sosX()`, `bolsin.esCmDestino(cm)`), no
   pedir el dato con un getter y decidir afuera.
5. Nunca devolver entidades a la pantalla: records `...Response` / `...Request` en `dto`.

## Patrón State (Documentación)
6. Documentacion es el contexto: delega en su estado actual cada mensaje que depende del estado y
   se pasa a sí misma como parámetro.
7. El estado abstracto declara todos los métodos dependientes del estado con comportamiento por
   defecto (rechazar la transición). Cada estado concreto redefine sólo las transiciones que
   salen de él en la máquina de estados. Nada de `if`/`switch` sobre el nombre del estado.
8. El estado concreto que hace la transición cierra el CambioEstadoDocumentacion actual
   (`setFechaHoraFin`), crea el estado siguiente y el nuevo CambioEstadoDocumentacion con el
   empleado responsable, y se los asigna a la documentación (`setEstado`, `agregarCE`).

## Código
9. 100 % orientado a objetos, corto y sin repeticiones: nada de archivos largos para lo que se
   resuelve en pocas líneas.
10. Todo en español: clases, métodos, mensajes, commits.
11. Inyección por constructor. La hora sale del bean `Clock` (nunca `LocalDateTime.now()` suelto).
12. Las contraseñas y claves no se suben al chat.
13. Antes de cerrar una tarea: compilar (`mvnw clean install`) y probar el flujo en el front.
