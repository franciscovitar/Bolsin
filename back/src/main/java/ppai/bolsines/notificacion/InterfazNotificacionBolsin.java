package ppai.bolsines.notificacion;

/**
 * CU 29 Notificar recepción de bolsín, incluido en el CU 28. En la secuencia es el «include» hacia
 * "29. Notificar recepción de Bolsin" que hace el gestor desde llamarCasoUso29(). No es una clase de
 * los diagramas: es el componente que pide el RNF 5. El gestor depende de esta interfaz y la
 * implementación se puede cambiar (log, Gmail, Outlook) sin tocarlo (Bajo Acoplamiento, variaciones
 * protegidas).
 * Se invoca una vez por cada comisión médica de origen que envió documentación en el bolsín.
 */
public interface InterfazNotificacionBolsin {

    void notificarRecepcionBolsin(NotificacionRecepcionBolsin notificacion);
}
