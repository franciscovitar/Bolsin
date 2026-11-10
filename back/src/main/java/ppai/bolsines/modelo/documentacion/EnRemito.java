package ppai.bolsines.modelo.documentacion;

/**
 * «state» EnRemito. Patrón State, rol CONCRETE STATE.
 *
 * Sus transiciones son de otros CU y no se implementaron:
 * - incluirEnBolsin() (CU 19) y modificarBolsin() (CU 20) -> EnBolsinSaliente;
 * - removerDeRemito() (CU 17) y modificarRemito() (CU 16) -> Registrada;
 * - modificarRemito() (CU 16) -> EnRemito.
 *
 * En el diagrama de clases figuran incluirEnBolsin(), modificarBolsin(), modificarRemito(), new() y
 * removerDeRemito(). La clase existe porque el historial de la documentación (data.sql) registra este
 * estado.
 */
public class EnRemito extends EstadoDocumentacion {

    /** new(): EnRemito del diagrama de clases. */
    public EnRemito() {
        super("EnRemito");
    }
}
