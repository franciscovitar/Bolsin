package ppai.bolsines.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Respuesta de tomarSeleccionBolsin: lo que la pantalla recibió en
 * mostrarNumRemitosyDocumentacionAsociada y mostrarOpcionesRecepcionParaSeleccion.
 */
public record SeleccionBolsinResponse(List<RemitoConDocumentacion> remitos, List<String> opciones) {

    public record RemitoConDocumentacion(int nroRemito, List<DatosDocumentacion> documentacion) {

        /** Agrupa las filas [nroRemito, nroDocumentacion, asunto, tipo, areaCMCDestino] por remito. */
        public static List<RemitoConDocumentacion> agrupar(int[] nrosRemito, String[][] documentacion) {
            return Arrays.stream(nrosRemito)
                    .mapToObj(nro -> new RemitoConDocumentacion(nro, Arrays.stream(documentacion)
                            .filter(fila -> fila[0].equals(String.valueOf(nro)))
                            .map(fila -> new DatosDocumentacion(Integer.parseInt(fila[1]), fila[2], fila[3], fila[4]))
                            .toList()))
                    .toList();
        }
    }

    /**
     * nroDocumentacion identifica a cada documentación para marcarla (A3, A4 y A5). areaCMCDestino
     * es el área de la CMC registrada en el detalle del remito (null si no tiene).
     */
    public record DatosDocumentacion(int nroDocumentacion, String asunto, String tipo, String areaCMCDestino) {
    }
}
