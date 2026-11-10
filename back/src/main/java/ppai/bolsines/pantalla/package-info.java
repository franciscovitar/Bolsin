/**
 * Clases boundary (pantallas) del diagrama. Cada pantalla es un @RestController: un endpoint
 * por cada mensaje que el actor le manda a la pantalla, con el mismo nombre que en el diagrama
 * de secuencia (por ejemplo opcRegistrarRecepcionBolsin, tomarSeleccionBolsin). Los mensajes
 * mostrarX() del gestor a la pantalla son métodos de la pantalla: guardan lo que hay que mostrar
 * y el endpoint lo devuelve al front, que lo dibuja.
 */
package ppai.bolsines.pantalla;
