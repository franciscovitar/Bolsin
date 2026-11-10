package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/** Puede redirigirse a otra área de la CMC (CU 28) o volver a Registrada por el CU 31 (no implementado). */
public class RecibidaYRechazada extends EstadoDocumentacion {

    public RecibidaYRechazada() {
        super("RecibidaYRechazada");
    }

    @Override
    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new ParaRedirigir());
    }
}
