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
 * «entity» CambioEstadoDocumentación del diagrama de clases (lifelines ":CambioEstadoDocumentacion",
 * "Actual:CambioEstadoDocumentacion" y "Nuevo:CambioEstadoDocumentacion" de la secuencia).
 *
 * Es un registro del historial de la documentación: qué estado tuvo, desde cuándo (fechaHoraInicio),
 * hasta cuándo (fechaHoraFin; el actual no tiene fin) y qué empleado hizo el cambio (asociación con
 * Empleado, responsable, que pide la ERS). Lo crea y lo cierra el estado concreto que hace la
 * transición (ver EstadoDocumentacion.cambiarEstado).
 *
 * Los getters de paquete existen sólo para que las pruebas verifiquen fechas y responsable; no son
 * operaciones del diagrama.
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

    /** Secuencia: new(fhInicio: DateTime, e: EstadoDocumentacion, responsable: Empleado) -> Nuevo:CambioEstadoDocumentacion. */
    public CambioEstadoDocumentacion(LocalDateTime fhInicio, EstadoDocumentacion e, Empleado responsable) {
        this.fechaHoraInicio = fhInicio;
        this.estado = e;
        this.responsableCE = responsable;
    }

    /** Secuencia: EnBolsinEnviado -> Actual:CambioEstadoDocumentacion setFechaHoraFin(fechaHoraFin). Cierra el actual. */
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    /** Secuencia: *sosActual(): boolean. El actual es el que todavía no tiene fechaHoraFin. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }
}
