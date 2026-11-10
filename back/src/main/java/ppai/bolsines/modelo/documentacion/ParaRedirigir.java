package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/** El área de la CMC verifica la documentación y la acepta o la rechaza. */
public class ParaRedirigir extends EstadoDocumentacion {

    public ParaRedirigir() {
        super("ParaRedirigir");
    }

    @Override
    public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYAceptada());
    }

    @Override
    public void recibirYRechazar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYRechazada());
    }
}
