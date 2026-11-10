package ppai.bolsines.modelo.documentacion;

/**
 * «state» RecibidaYAceptada ("RecibidayAceptada" en la máquina de estados). Patrón State, rol CONCRETE
 * STATE. Es el estado final de la máquina: no tiene transiciones salientes, así que no redefine nada y
 * todo pedido de cambio se rechaza por el comportamiento por defecto del estado abstracto.
 * En la secuencia es el objeto que crea EnBolsinEnviado con new(): RecibidayAceptada.
 */
public class RecibidaYAceptada extends EstadoDocumentacion {

    /** new(): RecibidaYAceptada del diagrama de clases. */
    public RecibidaYAceptada() {
        super("RecibidaYAceptada");
    }
}
