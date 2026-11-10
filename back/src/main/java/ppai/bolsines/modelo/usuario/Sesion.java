package ppai.bolsines.modelo.usuario;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sesión del modelo de dominio. En la secuencia aparece como Activa:Sesion.
 * El CU 28 usa la sesión vigente para conocer quién está registrando la recepción.
 * La demo deja esa sesión cargada en la base; no se implementó el inicio de sesión.
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

    /** En la secuencia: Gestor -> Activa:Sesion getUsuario(): Usuario, que a su vez envía getUsuario() a :Usuario. */
    public Usuario getUsuario() {
        return usuario.getUsuario();
    }
}
