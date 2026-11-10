package ppai.bolsines.modelo.usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ppai.bolsines.modelo.comisionmedica.ComisionMedica;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Empleado {

    @Id
    private int id;
    private String nombre;
    private String apellido;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "comision_medica_codigo")
    private ComisionMedica centroMedico;

    public boolean esTuUsuario(Usuario usuario) {
        return this.usuario.equals(usuario);
    }

    public ComisionMedica obtenerCM() {
        return centroMedico;
    }
}
