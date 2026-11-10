package ppai.bolsines.modelo.comisionmedica;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Comisión médica del modelo de dominio y de los diagramas del CU 28.
 * Se usa para identificar el origen y el destino de los bolsines y remitos.
 * Para A5 se agregó esCentral(), que identifica a la CMC por su nombre.
 * Ese criterio quedó pendiente de validación con la tutora.
 */
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

    /** En la secuencia: getNombre(): String (para mostrarCM y para el origen de cada bolsín). */
    public String getNombre() {
        return nombre;
    }

    /** Extensión CU 29: dirección a la que se notifica la recepción. */
    public String getEmail() {
        return email;
    }

    /**
     * Extensión A5 (no está en el diagrama): sólo la CMC redirige documentación a sus áreas. Se reconoce
     * por el nombre porque el modelo no tiene otro dato que la distinga (decisión 5 para la tutora).
     */
    public boolean esCentral() {
        return NOMBRE_CMC.equals(nombre);
    }
}
