package ppai.bolsines.modelo.documentacion;

/**
 * Estado concreto RecibidaYAceptada del patrón State.
 * La documentación termina en este estado cuando se acepta la recepción.
 * En la máquina de estados no tiene transiciones salientes.
 */
public class RecibidaYAceptada extends EstadoDocumentacion {

    /** new(): RecibidaYAceptada del diagrama de clases. */
    public RecibidaYAceptada() {
        super("RecibidaYAceptada");
    }
}
