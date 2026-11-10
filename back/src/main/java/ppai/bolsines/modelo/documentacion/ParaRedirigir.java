package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Estado concreto ParaRedirigir del patrón State.
 * En A5 se llega a él cuando la documentación debe pasar a otra área de la Central.
 * La máquina de estados permite que esa área luego la acepte o rechace.
 * La entrega interna posterior no forma parte del flujo implementado del CU 28.
 */
public class ParaRedirigir extends EstadoDocumentacion {

    /** new(): ParaRedirigir del diagrama de clases. */
    public ParaRedirigir() {
        super("ParaRedirigir");
    }

    /**
     * En la máquina de estados: ParaRedirigir -> RecibidayAceptada, "28.RegistrarRecepcionBolsin[el area verifica
     * que la documentacion es correcta]/recibiryAceptar()".
     */
    @Override
    public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYAceptada());
    }

    /**
     * En la máquina de estados: ParaRedirigir -> RecibidayRechazada, "28.RegistrarRecepcionBolsin[el area verifica
     * la documentacion y no es correcta]/recibiryRechazar()".
     */
    @Override
    public void recibirYRechazar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYRechazada());
    }
}
