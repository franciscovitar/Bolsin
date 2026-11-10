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

    @Getter(AccessLevel.PACKAGE)
    @Convert(converter = EstadoDocumentacionConverter.class)
    private EstadoDocumentacion estado;

    @Getter(AccessLevel.PACKAGE)
    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Empleado responsableCE;

    /** new(fhInicio, e, responsable) del diagrama. */
    public CambioEstadoDocumentacion(LocalDateTime fhInicio, EstadoDocumentacion e, Empleado responsable) {
        this.fechaHoraInicio = fhInicio;
        this.estado = e;
        this.responsableCE = responsable;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }
}
