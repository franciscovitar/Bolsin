package ppai.bolsines.modelo.documentacion;

/**
 * Estado concreto EnBolsinSaliente del patrón State.
 * Representa la documentación incluida en un bolsín antes de enviarlo.
 * Sus transiciones a EnBolsinEnviado o EnRemito pertenecen a otros casos de uso;
 * se conservan en el modelo, pero no se implementan aquí.
 */
public class EnBolsinSaliente extends EstadoDocumentacion {

    /** new(): EnBolsinSaliente del diagrama de clases. */
    public EnBolsinSaliente() {
        super("EnBolsinSaliente");
    }
}
