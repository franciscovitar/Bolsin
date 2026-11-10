package ppai.bolsines.modelo.bolsin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadoBolsin {

    @Id
    private int id;
    private String nombre;
    private String descripcion;

    public boolean sosEnviado() {
        return "Enviado".equals(nombre);
    }

    public boolean esRecibidoEnCMDestino() {
        return "RecibidoEnCMDestino".equals(nombre);
    }
}
