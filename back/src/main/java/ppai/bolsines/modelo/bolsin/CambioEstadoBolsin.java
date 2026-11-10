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

    /** new(fhInicio, e, usuarioResponsable) del diagrama. */
    public CambioEstadoBolsin(LocalDateTime fhInicio, EstadoBolsin e, Empleado usuarioResponsable) {
        this.fechaHoraInicio = fhInicio;
        this.estado = e;
        this.responsableCE = usuarioResponsable;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public boolean sosEnviado() {
        return estado.sosEnviado();
    }
}
