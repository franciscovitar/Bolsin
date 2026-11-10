package ppai.bolsines.modelo.remito;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Estado del remito del diagrama de clases.
 * Permite consultar si un remito está RecibidoYAceptado o RecibidoYAceptadoParcial.
 * No forma parte del patrón State: ese patrón se aplica a Documentación.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadoRemito {

    @Id
    private int id;
    private String nombre;
    private String descripcion;

    /** En la secuencia: loop de estados de remito, Gestor -> :EstadoRemito *esRecibidoyAceptado(): boolean. */
    public boolean esRecibidoyAceptado() {
        return "RecibidoYAceptado".equals(nombre);
    }

    /** Extensión A3/A4 (no está en el diagrama): el estado del remito al que no se le aceptó toda la documentación. */
    public boolean esRecibidoYAceptadoParcial() {
        return "RecibidoYAceptadoParcial".equals(nombre);
    }

    public String getNombre() {
        return nombre;
    }
}
