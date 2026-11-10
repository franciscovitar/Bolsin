package ppai.bolsines.modelo.documentacion;

/**
 * «state» EnBolsinSaliente. Patrón State, rol CONCRETE STATE.
 *
 * Sus transiciones son de otros CU y no se implementaron:
 * - enviar() (CU 27) -> EnBolsinEnviado;
 * - removerDeBolsin() (CU 21) -> EnRemito;
 * - modificarBolsin() (CU 20) -> EnBolsinSaliente.
 *
 * En el diagrama de clases figuran enviar(), modificarBolsin(), new() y removerDeBolsin(). La clase
 * existe porque el historial de la documentación (data.sql) registra este estado.
 */
public class EnBolsinSaliente extends EstadoDocumentacion {

    /** new(): EnBolsinSaliente del diagrama de clases. */
    public EnBolsinSaliente() {
        super("EnBolsinSaliente");
    }
}
