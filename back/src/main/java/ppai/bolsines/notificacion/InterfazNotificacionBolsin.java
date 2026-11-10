package ppai.bolsines.notificacion;

/**
 * Interfaz usada para la notificación de recepción del CU 29.
 * El Gestor depende de esta interfaz y no de una forma concreta de enviar avisos.
 * En esta implementación, la notificación se muestra en el log del backend.
 */
public interface InterfazNotificacionBolsin {

    void notificarRecepcionBolsin(NotificacionRecepcionBolsin notificacion);
}
