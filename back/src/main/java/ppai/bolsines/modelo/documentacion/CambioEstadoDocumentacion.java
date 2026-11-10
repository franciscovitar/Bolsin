package ppai.bolsines.modelo.documentacion;

import java.time.LocalDateTime;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ppai.bolsines.modelo.usuario.Empleado;

/**
 * Historial de estados de una documentación, según el diagrama de clases.
 * Guarda qué estado tuvo, cuándo comenzó y terminó, y qué empleado realizó
 * la transición. El cambio actual no tiene fecha de fin.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CambioEstadoDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter(AccessLevel.PACKAGE)
    private LocalDateTime fechaHoraInicio;
    @Getter(AccessLevel.PACKAGE)
    private LocalDateTime fechaHoraFin;

    /** El estado que se registra (asociación con EstadoDocumentación). Se guarda por nombre. */
    @Getter(AccessLevel.PACKAGE)
    @Convert(converter = EstadoDocumentacionConverter.class)
    private EstadoDocumentacion estado;

    /** Empleado responsable del cambio (en el modelo de dominio, el rol se llama responsableCE). */
    @Getter(AccessLevel.PACKAGE)
    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Empleado responsableCE;

    /** En la secuencia: new(fhInicio: DateTime, e: EstadoDocumentacion, responsable: Empleado) -> Nuevo:CambioEstadoDocumentacion. */
    public CambioEstadoDocumentacion(LocalDateTime fhInicio, EstadoDocumentacion e, Empleado responsable) {
        this.fechaHoraInicio = fhInicio;
        this.estado = e;
        this.responsableCE = responsable;
    }

    /** En la secuencia: EnBolsinEnviado -> Actual:CambioEstadoDocumentacion setFechaHoraFin(fechaHoraFin). Cierra el actual. */
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    /** En la secuencia: *sosActual(): boolean. El actual es el que todavía no tiene fechaHoraFin. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }
}
