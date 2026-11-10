package ppai.bolsines.modelo.documentacion;

/**
 * «state» NoRecibida. Patrón State, rol CONCRETE STATE. A este estado llega la documentación en A3
 * (EnBolsinEnviado -> NoRecibida).
 *
 * Sus transiciones son de otros CU y no se implementaron: revisarDocumentacion() -> Registrada (CU 31) y
 * darDeBaja() -> DeBaja (CU 9). En el diagrama de clases figuran darDeBaja(), new() y
 * revisarDocumentacion().
 */
public class NoRecibida extends EstadoDocumentacion {

    /** new(): NoRecibida del diagrama de clases. */
    public NoRecibida() {
        super("NoRecibida");
    }
}
