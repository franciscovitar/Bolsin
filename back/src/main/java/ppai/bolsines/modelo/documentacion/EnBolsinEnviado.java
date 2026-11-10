package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Estado concreto desde el que salen las cuatro transiciones del CU 28. Cada una crea el estado
 * siguiente y usa cambiarEstado(), heredado de EstadoDocumentacion.
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    public EnBolsinEnviado() {
        super("EnBolsinEnviado");
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

    @Override
    public void registrarNoRecibida(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new NoRecibida());
    }

    @Override
    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new ParaRedirigir());
    }
}
