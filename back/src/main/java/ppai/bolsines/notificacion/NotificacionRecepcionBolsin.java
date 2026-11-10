package ppai.bolsines.notificacion;

import java.util.Arrays;
import java.util.List;

/**
 * Datos de la notificación del CU 29 para una comisión médica de origen.
 * Incluye el bolsín recibido, su destino y la situación de la documentación
 * que corresponde a esa comisión. Es una ampliación del flujo dibujado en la secuencia.
 */
public record NotificacionRecepcionBolsin(String emailCMOrigen, String nombreCMOrigen, int nroPrecinto,
        String nombreCMDestino, List<SituacionDocumentacion> documentacion) {

    /** Situación de una documentación: remito, número, asunto y nombre de su estado final. */
    public record SituacionDocumentacion(int nroRemito, int nroDocumentacion, String asunto, String estado) {
    }

    /** Arma la notificación a partir de las filas [nroRemito, nroDocumentacion, asunto, estado]. */
    public static NotificacionRecepcionBolsin de(String emailCMOrigen, String nombreCMOrigen, int nroPrecinto,
            String nombreCMDestino, String[][] situacion) {
        return new NotificacionRecepcionBolsin(emailCMOrigen, nombreCMOrigen, nroPrecinto, nombreCMDestino,
                Arrays.stream(situacion)
                        .map(fila -> new SituacionDocumentacion(Integer.parseInt(fila[0]), Integer.parseInt(fila[1]),
                                fila[2], fila[3]))
                        .toList());
    }
}
