package ppai.bolsines.modelo.remito;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Estados de remito (sin historial, como en el modelo de dominio). */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadoRemito {

    @Id
    private int id;
    private String nombre;
    private String descripcion;

    public boolean esRecibidoyAceptado() {
        return "RecibidoYAceptado".equals(nombre);
    }

    /** A3: el remito al que le faltó documentación. */
    public boolean esRecibidoYAceptadoParcial() {
        return "RecibidoYAceptadoParcial".equals(nombre);
    }

    public String getNombre() {
        return nombre;
    }
}
