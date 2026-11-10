package ppai.bolsines.modelo.documentacion;

/**
 * Estado concreto NoRecibida del patrón State.
 * La documentación llega a este estado cuando, en A3, el encargado indica que no llegó.
 * La máquina de estados también muestra su revisión o baja, pero esos cambios pertenecen
 * a otros casos de uso y no se implementan en el CU 28.
 */
public class NoRecibida extends EstadoDocumentacion {

    /** new(): NoRecibida del diagrama de clases. */
    public NoRecibida() {
        super("NoRecibida");
    }
}
