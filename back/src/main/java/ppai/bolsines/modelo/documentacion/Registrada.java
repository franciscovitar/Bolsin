package ppai.bolsines.modelo.documentacion;

/**
 * Estado concreto Registrada del patrón State.
 * Es el estado inicial de la documentación. La máquina de estados muestra desde aquí
 * incluirEnRemito() y darDeBaja(), que corresponden a otros casos de uso.
 * No se implementan porque este trabajo se limita al CU 28.
 */
public class Registrada extends EstadoDocumentacion {

    /** new(): Registrada del diagrama de clases. */
    public Registrada() {
        super("Registrada");
    }
}
