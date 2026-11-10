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

/**
 * DetalleRemito del diagrama de secuencia (DetallesRemito en el de clases).
 * Conoce la documentación incluida y, cuando corresponde, el área de destino
 * dentro de la CMC. Por Experto (GRASP), determina qué operación pedirle a
 * Documentacion según lo que marcó el encargado en A3, A4 o A5.
 */
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

    /**
     * En la secuencia: Remito -> :DetalleRemito buscarDatosDocumentacion(): String[], que envía getAsunto() y
     * getNombreTipo() a :Documentacion. En el diagrama devuelve [asunto, tipo]; acá devuelve
     * [nroDocumentacion, asunto, tipo, areaCMCDestino (puede ser null)]: el número y el área son
     * extensiones de A3/A4/A5.
     */
    public String[] buscarDatosDocumentacion() {
        return new String[] { String.valueOf(documentacion.getNumero()), documentacion.getAsunto(),
                documentacion.getNombreTipo(), areaCMCDestino };
    }

    /** Extensión CU 29 (no está en el diagrama): devuelve [nroDocumentacion, asunto, estado actual]. */
    public String[] buscarSituacionDocumentacion() {
        return new String[] { String.valueOf(documentacion.getNumero()), documentacion.getAsunto(),
                documentacion.getNombreEstado() };
    }

    /**
     * En la secuencia: Remito -> :DetalleRemito actualizarDocumentacion(fhActual: DateTime, responsable: Empleado)
     * y DetalleRemito -> :Documentacion recibirYAceptar(fhActual, responsable).
     * Diferencia por A3, A4 y A5: recibe además nrosNoRecibida, nrosRechazada y nrosParaRedirigir, y según
     * lo que marcó el EB envía registrarNoRecibida (A3), recibirYRechazar (A4) o redirigir (A5). Las
     * condiciones son sobre los datos que marcó el EB, no sobre el nombre de un estado.
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
     * Extensión A3/A4 (no está en el diagrama): ¿el EB la marcó como no recibida (A3) o como que no
     * corresponde al destino (A4)? La que se redirige (A5) sí corresponde a la CMC: no cuenta como no
     * aceptada.
     */
    public boolean esNoAceptada(int[] nrosNoRecibida, int[] nrosRechazada) {
        return documentacion.estaEntre(nrosNoRecibida) || documentacion.estaEntre(nrosRechazada);
    }
}
