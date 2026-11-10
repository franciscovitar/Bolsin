package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Estado abstracto del patrón State.
 *
 * Declara las transiciones del CU 28 con un comportamiento por defecto: rechazar la transición,
 * porque desde la mayoría de los estados no está permitida. Cada estado concreto redefine sólo
 * las transiciones que salen de él en la máquina de estados. Las transiciones de otros casos de
 * uso (darDeBaja, enviar, incluirEnRemito, etc.) no se implementan: el PPAI implementa sólo el
 * CU 28.
 *
 * cambiarEstado() es el código común a todas las transiciones (reuso por herencia): cierra el
 * cambio de estado actual, crea el nuevo y se lo asigna a la documentación.
 */
public abstract class EstadoDocumentacion {

    private final String nombre;

    protected EstadoDocumentacion(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("recibir y aceptar");
    }

    public void recibirYRechazar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("recibir y rechazar");
    }

    public void registrarNoRecibida(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("registrar como no recibida");
    }

    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("redirigir");
    }

    protected void cambiarEstado(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable, EstadoDocumentacion nuevo) {
        for (CambioEstadoDocumentacion ce : historial) {
            if (ce.sosActual()) {
                ce.setFechaHoraFin(fhActual);
            }
        }
        CambioEstadoDocumentacion ce = crearCambioEstado(fhActual, nuevo, responsable);
        doc.setEstado(nuevo);
        doc.agregarCE(ce);
    }

    protected CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fhInicio, EstadoDocumentacion nuevo,
            Empleado responsable) {
        return new CambioEstadoDocumentacion(fhInicio, nuevo, responsable);
    }

    private IllegalStateException transicionInvalida(String accion) {
        return new IllegalStateException("No se puede " + accion + " una documentación en estado " + nombre);
    }
}
