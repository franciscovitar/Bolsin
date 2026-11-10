package ppai.bolsines.modelo.bolsin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * «entity» EstadoBolsin del diagrama de clases (lifeline ":EstadoBolsin"). Es una entidad catálogo, como
 * en el modelo de dominio: el patrón State se aplicó sólo a Documentación (consigna del grupo), por eso
 * acá el estado se reconoce por su nombre (Experto: el estado sabe qué es).
 * En el diagrama de clases también figuran esRecibidoyAceptada() y esRecibidoyAceptado(); corresponden
 * a EstadoRemito, que es a quien se lo pregunta la secuencia (errata del diagrama).
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadoBolsin {

    @Id
    private int id;
    private String nombre;
    private String descripcion;

    /** Secuencia: Actual:CambioEstadoBolsin -> :EstadoBolsin sosEnviado(): boolean. */
    public boolean sosEnviado() {
        return "Enviado".equals(nombre);
    }

    /** Secuencia: loop "Estado Bolsin", Gestor -> :EstadoBolsin *esRecibidoEnCMDestino(): boolean. */
    public boolean esRecibidoEnCMDestino() {
        return "RecibidoEnCMDestino".equals(nombre);
    }
}
