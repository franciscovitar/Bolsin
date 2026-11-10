package ppai.bolsines.modelo.documentacion;

/**
 * Estado concreto DeBaja del patrón State.
 * La documentación llega a este estado cuando se la da de baja desde otros CU.
 * No tiene transiciones adicionales implementadas para registrar una recepción.
 */
public class DeBaja extends EstadoDocumentacion {

    /** new(): DeBaja del diagrama de clases. */
    public DeBaja() {
        super("DeBaja");
    }
}
