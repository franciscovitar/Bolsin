package ppai.bolsines.notificacion;

import java.util.Arrays;
import java.util.List;

/**
 * CU 29: lo que se le notifica a UNA comisión médica de origen: el bolsín recibido, la CM destino y
 * la situación (estado final) de la documentación que esa CM envió. No incluye documentación de
 * otras CM de origen.
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
