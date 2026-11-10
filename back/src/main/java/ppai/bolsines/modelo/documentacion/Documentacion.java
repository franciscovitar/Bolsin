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
 * «entity» Documentación del diagrama de clases y lifeline ":Documentacion" de la secuencia.
 *
 * Patrón State, rol CONTEXT: es la clase cuyo comportamiento cambia según su estado. No tiene ningún
 * if sobre el estado; cada mensaje que depende del estado lo delega en estadoActual, pasándose a sí
 * misma ("this") y su historial, tal como dibuja la secuencia:
 *   Documentacion -> EnBolsinEnviado: recibirYAceptar(this, historial, fhActual, responsable).
 *
 * Asociaciones del diagrama de clases: tipoDocumento (1), estadoActual (1) y cambioEstado (1..*).
 * Operaciones del diagrama que NO están acá porque pertenecen a otros CU: cancelarRemito, darDeBaja,
 * enviar, incluirEnBolsin, incluirEnRemito, modificarBolsin, modificarRemito, removerDeBolsin,
 * removerDeRemito y revisarDocumentacion (decisión pendiente, ver docs/decisiones_para_la_tutora.md).
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

    /** Asociación estadoActual (1) hacia EstadoDocumentación. Se guarda por nombre (EstadoDocumentacionConverter). */
    @Getter(AccessLevel.PACKAGE)
    @Column(name = "estado_actual")
    @Convert(converter = EstadoDocumentacionConverter.class)
    private EstadoDocumentacion estadoActual;

    /** Asociación cambioEstado (1..*): el "historial" que la secuencia le pasa al estado. */
    @Getter(AccessLevel.PACKAGE)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "documentacion_numero")
    private List<CambioEstadoDocumentacion> cambioEstado = new ArrayList<>();

    /** new() del diagrama de clases. */
    public Documentacion(int numero, String asunto, LocalDate fechaPase, TipoDocumento tipoDocumento) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
    }

    /**
     * Extensión A3, A4 y A5 (no está en el diagrama): identifica sin ambigüedad a la documentación que
     * el EB marca (el número es su clave; el asunto se puede repetir).
     */
    public int getNumero() {
        return numero;
    }

    /** Extensión A3, A4 y A5 (no está en el diagrama): ¿su número está entre los que marcó el EB? */
    public boolean estaEntre(int[] nrosDocumentacion) {
        for (int nro : nrosDocumentacion) {
            if (nro == numero) {
                return true;
            }
        }
        return false;
    }

    /** Secuencia: DetalleRemito -> :Documentacion getAsunto(). */
    public String getAsunto() {
        return asunto;
    }

    /**
     * Extensión CU 29 (no está en el diagrama): nombre del estado actual, para informar la situación de
     * la documentación. Sólo lee el nombre que ya tiene el estado; no agrega lógica al State.
     */
    public String getNombreEstado() {
        return estadoActual.getNombre();
    }

    /** Secuencia: DetalleRemito -> :Documentacion getNombreTipo(), que a su vez envía getNombre() a :TipoDocumento. */
    public String getNombreTipo() {
        return tipoDocumento.getNombre();
    }

    // Transiciones del CU 28 (en el diagrama de clases: recibiryAceptar, recibiryRechazar, redirigir y
    // registrarNoRecibida, con parámetros (fhActual, responsable)). La documentación sólo delega en su
    // estado actual; si el estado no admite la transición, el estado la rechaza.

    /**
     * Secuencia: DetalleRemito -> :Documentacion recibirYAceptar(fhActual, responsable) y
     * Documentacion -> EnBolsinEnviado recibirYAceptar(this, historial, fhActual, responsable).
     * Máquina de estados: EnBolsinEnviado -> RecibidayAceptada y ParaRedirigir -> RecibidayAceptada.
     */
    public void recibirYAceptar(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.recibirYAceptar(this, cambioEstado, fhActual, responsable);
    }

    /**
     * No está en la secuencia (que modela el flujo descripto); lo usa A4. Mismo esquema que recibirYAceptar.
     * Máquina de estados: EnBolsinEnviado -> RecibidayRechazada y ParaRedirigir -> RecibidayRechazada.
     */
    public void recibirYRechazar(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.recibirYRechazar(this, cambioEstado, fhActual, responsable);
    }

    /** No está en la secuencia; lo usa A3. Máquina de estados: EnBolsinEnviado -> NoRecibida. */
    public void registrarNoRecibida(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.registrarNoRecibida(this, cambioEstado, fhActual, responsable);
    }

    /**
     * No está en la secuencia; lo usa A5. Máquina de estados: EnBolsinEnviado -> ParaRedirigir y
     * RecibidayRechazada -> ParaRedirigir.
     */
    public void redirigir(LocalDateTime fhActual, Empleado responsable) {
        estadoActual.redirigir(this, cambioEstado, fhActual, responsable);
    }

    /** Secuencia: EnBolsinEnviado -> :Documentacion setEstado(e: EstadoDocumentacion). Lo llama el estado. */
    public void setEstado(EstadoDocumentacion e) {
        this.estadoActual = e;
    }

    /** Secuencia: EnBolsinEnviado -> :Documentacion agregarCE(ce: CambioEstadoDocumentacion). Lo llama el estado. */
    public void agregarCE(CambioEstadoDocumentacion ce) {
        cambioEstado.add(ce);
    }
}
