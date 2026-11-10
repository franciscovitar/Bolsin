package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Estado concreto RecibidaYRechazada del patrón State.
 * Se usa en A4 cuando una documentación no corresponde a la comisión médica destino.
 * El diagrama permite redirigirla a otra área o revisarla. Se implementó redirigir()
 * porque esa transición también está dibujada para el CU 28; revisarDocumentacion()
 * corresponde a otro caso de uso.
 */
public class RecibidaYRechazada extends EstadoDocumentacion {

    /** new(): RecibidaYRechazada del diagrama de clases. */
    public RecibidaYRechazada() {
        super("RecibidaYRechazada");
    }

    /** En la máquina de estados: RecibidayRechazada -> ParaRedirigir, "28.RegistrarRecepcionBolsin/redirigir()". */
    @Override
    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new ParaRedirigir());
    }
}
