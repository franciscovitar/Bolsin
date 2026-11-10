package ppai.bolsines.modelo.documentacion;

/**
 * Estado concreto EnRemito del patrón State.
 * Representa documentación ya incluida en un remito. Las transiciones que aparecen
 * desde este estado corresponden a la generación o modificación de remitos y bolsines,
 * que están fuera del CU 28.
 */
public class EnRemito extends EstadoDocumentacion {

    /** new(): EnRemito del diagrama de clases. */
    public EnRemito() {
        super("EnRemito");
    }
}
