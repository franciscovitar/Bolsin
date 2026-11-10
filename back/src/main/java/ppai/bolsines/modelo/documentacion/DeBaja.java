package ppai.bolsines.modelo.documentacion;

/**
 * «state» DeBaja. Patrón State, rol CONCRETE STATE. Es un estado final de la máquina de estados
 * (se llega desde Registrada o NoRecibida con "9.EliminarDocumentacion/darDeBaja()", CU 9, no
 * implementado). En el diagrama de clases sólo figura new(): DeBaja.
 */
public class DeBaja extends EstadoDocumentacion {

    /** new(): DeBaja del diagrama de clases. */
    public DeBaja() {
        super("DeBaja");
    }
}
