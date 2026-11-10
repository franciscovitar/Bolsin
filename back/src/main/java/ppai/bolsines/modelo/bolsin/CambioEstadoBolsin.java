package ppai.bolsines.modelo.bolsin;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ppai.bolsines.modelo.usuario.Empleado;

/**
 * «entity» CambioEstadoBolsín del diagrama de clases (lifelines ":CambioEstadoBolsin",
 * "Actual:CambioEstadoBolsin" y "Nuevo:CambioEstadoBolsin" de la secuencia). Historial del bolsín:
 * estado, fechaHoraInicio, fechaHoraFin (el actual no tiene fin) y empleado responsable (rol
 * responsableCE del modelo de dominio).
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CambioEstadoBolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "estado_id")
    private EstadoBolsin estado;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Empleado responsableCE;

    /** Secuencia: new(fhInicio, e, usuarioResponsable) -> Nuevo:CambioEstadoBolsin. */
    public CambioEstadoBolsin(LocalDateTime fhInicio, EstadoBolsin e, Empleado usuarioResponsable) {
        this.fechaHoraInicio = fhInicio;
        this.estado = e;
        this.responsableCE = usuarioResponsable;
    }

    /** Secuencia: selec:Bolsin -> Actual:CambioEstadoBolsin setFechaHoraFin(fhfin). */
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    /** Secuencia: Bolsin -> :CambioEstadoBolsin *sosActual(): boolean. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    /** Secuencia: Bolsin -> Actual:CambioEstadoBolsin sosEnviado(), que se lo pregunta a :EstadoBolsin. */
    public boolean sosEnviado() {
        return estado.sosEnviado();
    }
}
