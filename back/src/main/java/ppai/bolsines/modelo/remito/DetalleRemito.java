package ppai.bolsines.modelo.remito;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ppai.bolsines.modelo.documentacion.Documentacion;
import ppai.bolsines.modelo.usuario.Empleado;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetalleRemito {

    @Id
    private int id;
    @Column(name = "area_cmc_destino")
    private String areaCMCDestino;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "documentacion_numero")
    private Documentacion documentacion;

    /** Devuelve [nroDocumentacion, asunto, tipo de documento, área de la CMC destino (puede ser null)]. */
    public String[] buscarDatosDocumentacion() {
        return new String[] { String.valueOf(documentacion.getNumero()), documentacion.getAsunto(),
                documentacion.getNombreTipo(), areaCMCDestino };
    }

    /** CU 29: devuelve [nroDocumentacion, asunto, estado actual]. */
    public String[] buscarSituacionDocumentacion() {
        return new String[] { String.valueOf(documentacion.getNumero()), documentacion.getAsunto(),
                documentacion.getNombreEstado() };
    }

    /**
     * Flujo descripto: recibirYAceptar. Si el EB marcó esta documentación: registrarNoRecibida (A3),
     * recibirYRechazar (A4) o redirigir (A5). Qué transición es válida lo decide el estado actual (State).
     */
    public void actualizarDocumentacion(LocalDateTime fhActual, Empleado responsable, int[] nrosNoRecibida,
            int[] nrosRechazada, int[] nrosParaRedirigir) {
        if (documentacion.estaEntre(nrosNoRecibida)) {
            documentacion.registrarNoRecibida(fhActual, responsable);
        } else if (documentacion.estaEntre(nrosRechazada)) {
            documentacion.recibirYRechazar(fhActual, responsable);
        } else if (documentacion.estaEntre(nrosParaRedirigir)) {
            documentacion.redirigir(fhActual, responsable);
        } else {
            documentacion.recibirYAceptar(fhActual, responsable);
        }
    }

    /**
     * ¿El EB la marcó como no recibida (A3) o como que no corresponde al destino (A4)? La que se
     * redirige (A5) sí corresponde a la CMC: no cuenta como no aceptada.
     */
    public boolean esNoAceptada(int[] nrosNoRecibida, int[] nrosRechazada) {
        return documentacion.estaEntre(nrosNoRecibida) || documentacion.estaEntre(nrosRechazada);
    }
}
