package ppai.bolsines.modelo.usuario;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * «entity» Sesión del diagrama de clases (lifeline "Activa:Sesion" de la secuencia). El CU no incluye
 * iniciar sesión: la sesión activa (sin fechaHoraFin) viene cargada en data.sql.
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

    /** Secuencia: Gestor -> Activa:Sesion getUsuario(): Usuario, que a su vez envía getUsuario() a :Usuario. */
    public Usuario getUsuario() {
        return usuario.getUsuario();
    }
}
