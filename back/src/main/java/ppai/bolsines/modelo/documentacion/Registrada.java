package ppai.bolsines.modelo.documentacion;

/**
 * «state» Registrada. Patrón State, rol CONCRETE STATE. Estado inicial de la máquina de estados
 * ("7.RegistrarDocumentacion/new()").
 *
 * Sus transiciones son de otros CU y no se implementaron: incluirEnRemito() -> EnRemito (CU 15) y
 * darDeBaja() -> DeBaja (CU 9). En el diagrama de clases figuran darDeBaja(), incluirEnRemito() y new().
 * La clase existe porque el historial de la documentación (data.sql) registra este estado.
 */
public class Registrada extends EstadoDocumentacion {

    /** new(): Registrada del diagrama de clases. */
    public Registrada() {
        super("Registrada");
    }
}
