package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * «abstract» EstadoDocumentación del diagrama de clases.
 *
 * Patrón State, rol STATE (estado abstracto). Declara las transiciones del CU 28 con un comportamiento
 * por defecto: rechazar la transición (IllegalStateException, que la API devuelve como 409), porque
 * desde la mayoría de los estados no está permitida. Cada estado concreto (ConcreteState) redefine
 * sólo las transiciones que salen de él en la máquina de estados. Así no hace falta ningún if o switch
 * sobre el nombre del estado.
 *
 * cambiarEstado() y crearCambioEstado() son el código común a todas las transiciones (reuso por
 * herencia; en el diagrama, con visibilidad # protegida).
 *
 * Diferencia con el diagrama de clases: allí este abstracto también declara darDeBaja, enviar,
 * incluirEnBolsin, incluirEnRemito, modificarBolsin, modificarRemito, removerDeBolsin, removerDeRemito
 * y revisarDocumentacion. Son de otros CU y no se implementaron (decisión pendiente, ver
 * docs/decisiones_para_la_tutora.md, punto 10). En el diagrama, "nombre" es un atributo público;
 * acá es privado con getNombre().
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
     * Secuencia: EnBolsinEnviado self cambiarEstado(this, historial, fhActual, responsable, nuevo).
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
     * Secuencia: self crearCambioEstado(fhInicio, nuevo, responsable): CambioEstadoDocumentacion y
     * new(fhInicio, e, responsable) a Nuevo:CambioEstadoDocumentacion. GRASP Creador: el estado crea el
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
