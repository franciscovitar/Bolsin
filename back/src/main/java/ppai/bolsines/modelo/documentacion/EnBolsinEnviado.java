package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Estado concreto EnBolsinEnviado del patrón State.
 * La máquina de estados muestra cuatro posibles transiciones para el CU 28:
 * aceptar, rechazar, registrar como no recibida o redirigir a otra área.
 * El flujo normal usa recibirYAceptar(); A3, A4 y A5 usan las otras operaciones.
 * Todas registran el cambio mediante cambiarEstado(), heredado del abstracto.
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    /** new(): EnBolsinEnviado del diagrama de clases. */
    public EnBolsinEnviado() {
        super("EnBolsinEnviado");
    }

    /**
     * En la máquina de estados: EnBolsinEnviado -> RecibidayAceptada, "28.RegistrarRecpcionBolsin[La documentacion
     * corresponde a la cm destino]/recibiryAceptar()". Flujo descripto y la documentación no marcada en A3/A4/A5.
     * En la secuencia: new(): RecibidayAceptada y mensaje a sí mismo cambiarEstado(...).
     */
    @Override
    public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYAceptada());
    }

    /**
     * En la máquina de estados: EnBolsinEnviado -> RecibidayRechazada, "28.RegistrarRecepecionBolsin[la documentacion
     * no corresponde a la cm destino]/recibiryRechazar()". Flujo A4 (no está en la secuencia).
     */
    @Override
    public void recibirYRechazar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYRechazada());
    }

    /**
     * En la máquina de estados: EnBolsinEnviado -> NoRecibida, "28.RegistrarRecepcionBolsin[no se recibio]/
     * registrarNoRecibida()". Flujo A3 (no está en la secuencia).
     */
    @Override
    public void registrarNoRecibida(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new NoRecibida());
    }

    /**
     * En la máquina de estados: EnBolsinEnviado -> ParaRedirigir, "28.RegistrarRecepcionBolsin[la CM es destino es
     * CMC y se debe redirigir a otra area]/redirigir()". Flujo A5 (no está en la secuencia). La guarda "la CM
     * destino es la CMC" la controla el gestor preguntándole al bolsín antes de llegar acá.
     */
    @Override
    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new ParaRedirigir());
    }
}
