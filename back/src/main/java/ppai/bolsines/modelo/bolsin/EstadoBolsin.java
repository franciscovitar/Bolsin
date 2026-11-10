package ppai.bolsines.modelo.bolsin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Estado del bolsín del diagrama de clases.
 * Se usa para consultar si un bolsín está Enviado o RecibidoEnCMDestino.
 * Es un estado de catálogo; el patrón State del TP se aplica a Documentación.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadoBolsin {

    @Id
    private int id;
    private String nombre;
    private String descripcion;

    /** En la secuencia: Actual:CambioEstadoBolsin -> :EstadoBolsin sosEnviado(): boolean. */
    public boolean sosEnviado() {
        return "Enviado".equals(nombre);
    }

    /** En la secuencia: loop "Estado Bolsin", Gestor -> :EstadoBolsin *esRecibidoEnCMDestino(): boolean. */
    public boolean esRecibidoEnCMDestino() {
        return "RecibidoEnCMDestino".equals(nombre);
    }
}
