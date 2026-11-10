package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * «state» ParaRedirigir. Patrón State, rol CONCRETE STATE. A este estado llega la documentación en A5
 * (EnBolsinEnviado -> ParaRedirigir). Después, el área de la CMC verifica la documentación y la acepta o
 * la rechaza. Esa verificación es posterior a la recepción: ningún flujo implementado del CU 28 la
 * dispara (está fuera del alcance), pero las transiciones existen como en el diagrama de clases
 * (recibirYAceptar y recibirYRechazar) y tienen prueba unitaria.
 */
public class ParaRedirigir extends EstadoDocumentacion {

    /** new(): ParaRedirigir del diagrama de clases. */
    public ParaRedirigir() {
        super("ParaRedirigir");
    }

    /**
     * Máquina de estados: ParaRedirigir -> RecibidayAceptada, "28.RegistrarRecepcionBolsin[el area verifica
     * que la documentacion es correcta]/recibiryAceptar()".
     */
    @Override
    public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYAceptada());
    }

    /**
     * Máquina de estados: ParaRedirigir -> RecibidayRechazada, "28.RegistrarRecepcionBolsin[el area verifica
     * la documentacion y no es correcta]/recibiryRechazar()".
     */
    @Override
    public void recibirYRechazar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYRechazada());
    }
}
