package ppai.bolsines.notificacion;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación de demostración del CU 29 (no está en los diagramas): redacta el correo y lo registra
 * en el log en lugar de enviarlo. Cambiarla por un envío real no afecta al gestor ni al modelo.
 */
@Component
public class NotificacionBolsinLog implements InterfazNotificacionBolsin {

    private static final Logger log = LoggerFactory.getLogger(NotificacionBolsinLog.class);

    @Override
    public void notificarRecepcionBolsin(NotificacionRecepcionBolsin notificacion) {
        log.info(redactar(notificacion));
    }

    /** Texto del correo: el bolsín recibido y una línea por cada documentación con su estado final. */
    String redactar(NotificacionRecepcionBolsin n) {
        return "CU 29 - Correo a " + n.emailCMOrigen() + " (" + n.nombreCMOrigen() + "): el bolsín con precinto "
                + n.nroPrecinto() + " fue recibido en " + n.nombreCMDestino() + ". Situación de la documentación enviada:"
                + n.documentacion().stream()
                        .map(d -> "\n  - Remito " + d.nroRemito() + ", documentación " + d.nroDocumentacion()
                                + " \"" + d.asunto() + "\": " + d.estado())
                        .collect(Collectors.joining());
    }
}
