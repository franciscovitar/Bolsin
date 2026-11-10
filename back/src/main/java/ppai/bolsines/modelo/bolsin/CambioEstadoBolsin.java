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
 * Registra el historial de estados de un bolsín, como en el diagrama de clases.
 * Guarda el estado, las fechas de inicio y fin, y el empleado responsable.
 * El cambio que todavía está vigente no tiene fecha de fin.
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

    /** En la secuencia: new(fhInicio, e, usuarioResponsable) -> Nuevo:CambioEstadoBolsin. */
    public CambioEstadoBolsin(LocalDateTime fhInicio, EstadoBolsin e, Empleado usuarioResponsable) {
        this.fechaHoraInicio = fhInicio;
        this.estado = e;
        this.responsableCE = usuarioResponsable;
    }

    /** En la secuencia: selec:Bolsin -> Actual:CambioEstadoBolsin setFechaHoraFin(fhfin). */
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    /** En la secuencia: Bolsin -> :CambioEstadoBolsin *sosActual(): boolean. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    /** En la secuencia: Bolsin -> Actual:CambioEstadoBolsin sosEnviado(), que se lo pregunta a :EstadoBolsin. */
    public boolean sosEnviado() {
        return estado.sosEnviado();
    }
}
