package ppai.bolsines.modelo.remito;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * «entity» EstadoRemito del diagrama de clases (lifeline ":EstadoRemito"). Entidad catálogo, sin
 * historial, como en el modelo de dominio; el estado se reconoce por su nombre (el patrón State se
 * aplicó sólo a Documentación).
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadoRemito {

    @Id
    private int id;
    private String nombre;
    private String descripcion;

    /** Secuencia: loop de estados de remito, Gestor -> :EstadoRemito *esRecibidoyAceptado(): boolean. */
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
