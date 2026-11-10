package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;
import java.util.List;

import ppai.bolsines.modelo.usuario.Empleado;

/**
 * «state» EnBolsinEnviado. Patrón State, rol CONCRETE STATE. Es el estado de la documentación cuando
 * llega el bolsín, y desde él salen las cuatro transiciones del CU 28 de la máquina de estados.
 * En la secuencia es la lifeline "EnBolsinEnviado" que recibe recibirYAceptar(this, historial, ...).
 *
 * Cada transición crea el estado siguiente (new() en la secuencia, GRASP Creador) y usa cambiarEstado(),
 * heredado de EstadoDocumentacion. Quién elige cuál se llama es DetalleRemito, según lo que marcó el EB.
 *
 * Diferencia con el diagrama de clases: allí recibirYAceptar figura con retorno RecibidaYAceptada; en el
 * abstracto y en el código es void (errata del diagrama).
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    /** new(): EnBolsinEnviado del diagrama de clases. */
    public EnBolsinEnviado() {
        super("EnBolsinEnviado");
    }

    /**
     * Máquina de estados: EnBolsinEnviado -> RecibidayAceptada, "28.RegistrarRecpcionBolsin[La documentacion
     * corresponde a la cm destino]/recibiryAceptar()". Flujo descripto y la documentación no marcada en A3/A4/A5.
     * Secuencia: new(): RecibidayAceptada y self cambiarEstado(...).
     */
    @Override
    public void recibirYAceptar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYAceptada());
    }

    /**
     * Máquina de estados: EnBolsinEnviado -> RecibidayRechazada, "28.RegistrarRecepecionBolsin[la documentacion
     * no corresponde a la cm destino]/recibiryRechazar()". Flujo A4 (no está en la secuencia).
     */
    @Override
    public void recibirYRechazar(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new RecibidaYRechazada());
    }

    /**
     * Máquina de estados: EnBolsinEnviado -> NoRecibida, "28.RegistrarRecepcionBolsin[no se recibio]/
     * registrarNoRecibida()". Flujo A3 (no está en la secuencia).
     */
    @Override
    public void registrarNoRecibida(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new NoRecibida());
    }

    /**
     * Máquina de estados: EnBolsinEnviado -> ParaRedirigir, "28.RegistrarRecepcionBolsin[la CM es destino es
     * CMC y se debe redirigir a otra area]/redirigir()". Flujo A5 (no está en la secuencia). La guarda "la CM
     * destino es la CMC" la controla el gestor preguntándole al bolsín antes de llegar acá.
     */
    @Override
    public void redirigir(Documentacion doc, List<CambioEstadoDocumentacion> historial,
            LocalDateTime fhActual, Empleado responsable) {
        cambiarEstado(doc, historial, fhActual, responsable, new ParaRedirigir());
    }
}
