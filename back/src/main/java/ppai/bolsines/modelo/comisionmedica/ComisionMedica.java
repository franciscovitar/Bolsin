package ppai.bolsines.modelo.comisionmedica;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ComisionMedica {

    /** La CMC es una sola (ERS, glosario: "CMC: Comisión médica central"); se reconoce por su nombre. */
    private static final String NOMBRE_CMC = "Comisión Médica Central";

    @Id
    @EqualsAndHashCode.Include
    private int codigo;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    /** A5: sólo la CMC redirige documentación a sus áreas. */
    public boolean esCentral() {
        return NOMBRE_CMC.equals(nombre);
    }
}
