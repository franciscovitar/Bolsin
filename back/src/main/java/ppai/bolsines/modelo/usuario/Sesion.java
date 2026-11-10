package ppai.bolsines.modelo.usuario;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sesión del usuario logueado. El CU no incluye iniciar sesión: la sesión activa (sin
 * fechaHoraFin) viene cargada en data.sql.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sesion {

    @Id
    private int id;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario.getUsuario();
    }
}
