package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * «state» RecibidaYRechazada. Patrón State, rol CONCRETE STATE. A este estado llega la documentación
 * en A4 (EnBolsinEnviado -> RecibidayRechazada).
 *
 * Transiciones que salen de él en la máquina de estados:
 * - "28.RegistrarRecepcionBolsin/redirigir()" -> ParaRedirigir: implementada abajo. Ningún flujo del CU
 *   28 la usa hoy (sirve cuando un área de la CMC rechaza y la documentación va a otra área).
 * - "31.RegistrarRevisionDeDocumentacion/revisarDocumentacion()" -> Registrada: es del CU 31, no se
 *   implementó.
 *
 * Diferencia con el diagrama de clases: allí figura redirigir(): void sin parámetros; acá tiene la
 * firma del estado abstracto (doc, historial, fhActual, responsable), que es la que puede redefinir.
 */
public class RecibidaYRechazada extends EstadoDocumentacion {

    /** new(): RecibidaYRechazada del diagrama de clases. */
    public RecibidaYRechazada() {
        super("RecibidaYRechazada");
    }

    /** Máquina de estados: RecibidayRechazada -> ParaRedirigir, "28.RegistrarRecepcionBolsin/redirigir()". */
    @Override
    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new ParaRedirigir());
    }
}
