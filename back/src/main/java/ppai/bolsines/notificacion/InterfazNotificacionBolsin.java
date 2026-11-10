package ppai.bolsines.notificacion;

/**
 * CU 29 Notificar recepción de bolsín (incluido en el CU 28). Es el componente que pide el RNF 5:
 * el gestor depende de esta interfaz y la implementación se puede cambiar (log, Gmail, Outlook).
 * Se invoca una vez por cada comisión médica de origen que envió documentación en el bolsín.
 */
public interface InterfazNotificacionBolsin {

    void notificarRecepcionBolsin(NotificacionRecepcionBolsin notificacion);
}
