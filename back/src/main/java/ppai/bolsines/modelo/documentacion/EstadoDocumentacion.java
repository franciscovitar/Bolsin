package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Estado abstracto del patrón State, como en el diagrama de clases.
 * Define las operaciones que necesita el CU 28; si el estado concreto no permite
 * una transición, se rechaza. cambiarEstado() comparte la lógica de cerrar el estado
 * anterior y registrar el nuevo con fecha y responsable.
 *
 * El diagrama también muestra operaciones de otros CU que no se implementaron.
 * Esa diferencia está registrada en docs/diferencias_codigo_diagramas.md.
 */
public abstract class EstadoDocumentacion {

    private final String nombre;

    protected EstadoDocumentacion(String nombre) {
        this.nombre = nombre;
    }

    /** getNombre(): String del diagrama de clases. Lo usan la persistencia y el CU 29 para informar el estado. */
    public String getNombre() {
        return nombre;
    }

    /** recibirYAceptar(doc, historial, fhActual, responsable). Por defecto se rechaza; lo redefinen EnBolsinEnviado y ParaRedirigir. */
    public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("recibir y aceptar");
    }

    /** recibirYRechazar(doc, historial, fhActual, responsable). Por defecto se rechaza; lo redefinen EnBolsinEnviado y ParaRedirigir. */
    public void recibirYRechazar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("recibir y rechazar");
    }

    /** registrarNoRecibida(doc, historial, fhActual, responsable). Por defecto se rechaza; lo redefine EnBolsinEnviado. */
    public void registrarNoRecibida(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("registrar como no recibida");
    }

    /** redirigir(doc, historial, fhActual, responsable). Por defecto se rechaza; lo redefinen EnBolsinEnviado y RecibidaYRechazada. */
    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        throw transicionInvalida("redirigir");
    }

    /**
     * En la secuencia: EnBolsinEnviado mensaje a sí mismo cambiarEstado(this, historial, fhActual, responsable, nuevo).
     * Mismos parámetros y orden que el diagrama de clases (# cambiarEstado(doc, historial, fhActual,
     * responsable, nuevo)). Hace, en este orden, lo que dibuja la secuencia:
     * 1. *sosActual() a :CambioEstadoDocumentacion y setFechaHoraFin(fhActual) al Actual (lo cierra);
     * 2. crearCambioEstado(...), que crea el Nuevo:CambioEstadoDocumentacion con el responsable;
     * 3. setEstado(nuevo) y agregarCE(ce) a la documentación.
     */
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

    /**
     * En la secuencia: mensaje a sí mismo crearCambioEstado(fhInicio, nuevo, responsable): CambioEstadoDocumentacion y
     * new(fhInicio, e, responsable) a Nuevo:CambioEstadoDocumentacion. Por Creador (GRASP), el estado crea el
     * cambio de estado porque tiene los datos para inicializarlo.
     */
    protected CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fhInicio, EstadoDocumentacion nuevo,
            Empleado responsable) {
        return new CambioEstadoDocumentacion(fhInicio, nuevo, responsable);
    }

    private IllegalStateException transicionInvalida(String accion) {
        return new IllegalStateException("No se puede " + accion + " una documentación en estado " + nombre);
    }
}
