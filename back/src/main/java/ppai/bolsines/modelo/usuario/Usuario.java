package ppai.bolsines.modelo.usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    @Id
    @EqualsAndHashCode.Include
    private int id;
    private String nombre;
    private String contrasena;

    /** Mensaje getUsuario() que la sesión le envía al usuario en el diagrama de secuencia. */
    public Usuario getUsuario() {
        return this;
    }
}
