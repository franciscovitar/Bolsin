package ppai.bolsines.modelo.usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ppai.bolsines.modelo.comisionmedica.ComisionMedica;

/**
 * Empleado del modelo de dominio y de la secuencia del CU 28.
 * Permite reconocer qué empleado corresponde al usuario activo y a qué
 * comisión médica pertenece. Se registra como responsable de la recepción.
 */
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

    /** En la secuencia: Gestor -> :Empleado *esTuUsuario(u: Usuario): boolean. */
    public boolean esTuUsuario(Usuario usuario) {
        return this.usuario.equals(usuario);
    }

    /**
     * En la secuencia: Gestor -> Logueado:Empleado obtenerCM(): ComisionMedica. En el diagrama, Empleado le envía
     * además getNombre() a :ComisionMedica; acá devuelve la CM y el getNombre() lo envía el gestor.
     */
    public ComisionMedica obtenerCM() {
        return centroMedico;
    }
}
