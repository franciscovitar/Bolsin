package ppai.bolsines.dto;

import java.util.List;

/** Respuesta de opcRegistrarRecepcionBolsin: lo que la pantalla recibió en mostrarCM y mostrarBolsinesEnviadosParaSeleccion. */
public record OpcRegistrarRecepcionResponse(String nombreCM, List<BolsinParaSeleccion> bolsines) {

    public record BolsinParaSeleccion(int nroPrecinto, String origen) {
    }
}
