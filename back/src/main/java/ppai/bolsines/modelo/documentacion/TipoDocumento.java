package ppai.bolsines.modelo.documentacion;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TipoDocumento {

    @Id
    private int id;
    private String nombre;
    private String descripcion;

    public String getNombre() {
        return nombre;
    }
}
