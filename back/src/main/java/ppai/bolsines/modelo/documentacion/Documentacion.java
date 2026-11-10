package ppai.bolsines.modelo.documentacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Contexto del patrón State. Cada mensaje que depende del estado se delega en el estado actual,
 * pasándole la propia documentación y su historial de cambios de estado.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Documentacion {

    @Id
    private int numero;
    private String asunto;
    private LocalDate fechaPase;

    @ManyToOne
    @JoinColumn(name = "tipo_documento_id")
    private TipoDocumento tipoDocumento;

    @Getter(AccessLevel.PACKAGE)
    @Column(name = "estado_actual")
    @Convert(converter = EstadoDocumentacionConverter.class)
    private EstadoDocumentacion estadoActual;

    @Getter(AccessLevel.PACKAGE)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "documentacion_numero")
    private List<CambioEstadoDocumentacion> cambioEstado = new ArrayList<>();

    /** new() del diagrama. */
    public Documentacion(int numero, String asunto, LocalDate fechaPase, TipoDocumento tipoDocumento) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
    }

    /** A3 y A4: identifica sin ambigüedad a la documentación que el EB marca (el número es su clave). */
    public int getNumero() {
        return numero;
    }

    /** A3 y A4: ¿su número está entre los que marcó el EB? */
    public boolean estaEntre(int[] nrosDocumentacion) {
        for (int nro : nrosDocumentacion) {
            if (nro == numero) {
                return true;
            }
        }
        return false;
    }

    public String getAsunto() {
        return asunto;
    }

    /** CU 29: nombre del estado actual, para informar la situación de la documentación (sólo lectura). */
    public String getNombreEstado() {
        return estadoActual.getNombre();
    }

    public String getNombreTipo() {
        return tipoDocumento.getNombre();
    }

    // Transiciones del CU 28: la documentación sólo delega en su estado actual.

    public void recibirYAceptar(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.recibirYAceptar(this, cambioEstado, fhActual, responsable);
    }

    public void recibirYRechazar(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.recibirYRechazar(this, cambioEstado, fhActual, responsable);
    }

    public void registrarNoRecibida(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.registrarNoRecibida(this, cambioEstado, fhActual, responsable);
    }

    public void redirigir(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.redirigir(this, cambioEstado, fhActual, responsable);
    }

    public void setEstado(EstadoDocumentacion e) {
        this.estadoActual = e;
    }

    public void agregarCE(CambioEstadoDocumentacion ce) {
        cambioEstado.add(ce);
    }
}
