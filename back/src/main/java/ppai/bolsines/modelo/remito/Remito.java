package ppai.bolsines.modelo.remito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ppai.bolsines.modelo.comisionmedica.ComisionMedica;
import ppai.bolsines.modelo.usuario.Empleado;

/**
 * «entity» Remito del diagrama de clases y lifeline ":Remito" de la secuencia.
 *
 * GRASP Experto: conoce sus detalles (y por ellos su documentación), su estado y su CM de origen y
 * destino; arma los datos de su documentación y decide en qué estado queda al recibirse.
 * Asociaciones del diagrama: detallesRemito (1..*), estado (EstadoRemito), origen y destino
 * (ComisiónMédica). El estado del remito no tiene historial (como en el modelo de dominio).
 * Atributo: en el diagrama nroRemito; en el código numero.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Remito {

    @Id
    private int numero;
    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "estado_id")
    private EstadoRemito estado;

    @ManyToOne
    @JoinColumn(name = "origen_codigo")
    private ComisionMedica origen;

    @ManyToOne
    @JoinColumn(name = "destino_codigo")
    private ComisionMedica destino;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "remito_numero")
    private List<DetalleRemito> detalleRemito = new ArrayList<>();

    /** Secuencia: Bolsin -> :Remito *getNumero(): int. */
    public int getNumero() {
        return numero;
    }

    /**
     * Secuencia: Bolsin -> :Remito buscarDatosDocumentacion(): String[][] y el loop "Documentacion Remito"
     * con Remito -> :DetalleRemito buscarDatosDocumentacion(): String[]. Una fila [nroRemito,
     * nroDocumentacion, asunto, tipo, areaCMCDestino] por documentación (nroDocumentacion y
     * areaCMCDestino son extensiones de A3/A4/A5).
     */
    public String[][] buscarDatosDocumentacion() {
        List<String[]> filas = new ArrayList<>();
        for (DetalleRemito detalle : detalleRemito) {
            String[] datos = detalle.buscarDatosDocumentacion();
            filas.add(new String[] { String.valueOf(numero), datos[0], datos[1], datos[2], datos[3] });
        }
        return filas.toArray(String[][]::new);
    }

    /**
     * Extensión CU 29: getOrigen() no está en el diagrama, pero la asociación origen Remito -> ComisiónMédica
     * sí. La CM que generó el remito es la que envió su documentación.
     */
    public ComisionMedica getOrigen() {
        return origen;
    }

    /** Extensión CU 29 (no está en el diagrama): una fila [nroRemito, nroDocumentacion, asunto, estado] por documentación. */
    public String[][] buscarSituacionDocumentacion() {
        List<String[]> filas = new ArrayList<>();
        for (DetalleRemito detalle : detalleRemito) {
            String[] datos = detalle.buscarSituacionDocumentacion();
            filas.add(new String[] { String.valueOf(numero), datos[0], datos[1], datos[2] });
        }
        return filas.toArray(String[][]::new);
    }

    /**
     * Secuencia: Bolsin -> :Remito actualizarEstado(estadoRemito: EstadoRemito, fhActual: DateTime,
     * responsable: Empleado); luego self setEstado(e) y self actualizarDocumentacion(fhActual, responsable).
     * Diferencia por A3, A4 y A5: recibe al final estadoRemitoParcial, nrosNoRecibida, nrosRechazada y
     * nrosParaRedirigir. Si alguna documentación del remito no se aceptó (no recibida o rechazada), queda
     * RecibidoYAceptadoParcial; si no, RecibidoYAceptado. A3: regla de la ERS. A4 y A5 (la redirigida no
     * cuenta como no aceptada): decisiones provisionales del grupo, la ERS no lo dice.
     * En el flujo descripto los arreglos están vacíos y el comportamiento es el del diagrama.
     */
    public void actualizarEstado(EstadoRemito estadoRemito, LocalDateTime fhActual, Empleado responsable,
            EstadoRemito estadoRemitoParcial, int[] nrosNoRecibida, int[] nrosRechazada, int[] nrosParaRedirigir) {
        setEstado(tieneDocumentacionNoAceptada(nrosNoRecibida, nrosRechazada) ? estadoRemitoParcial : estadoRemito);
        actualizarDocumentacion(fhActual, responsable, nrosNoRecibida, nrosRechazada, nrosParaRedirigir);
    }

    /** Secuencia: Remito self setEstado(e: EstadoRemito). */
    public void setEstado(EstadoRemito e) {
        this.estado = e;
    }

    /** Extensión A3/A4 (no está en el diagrama): ¿algún detalle tiene documentación no aceptada? */
    private boolean tieneDocumentacionNoAceptada(int[] nrosNoRecibida, int[] nrosRechazada) {
        return detalleRemito.stream().anyMatch(detalle -> detalle.esNoAceptada(nrosNoRecibida, nrosRechazada));
    }

    /**
     * Secuencia: self actualizarDocumentacion(fhActual, responsable) y el loop "ActualizarEstadosDocumentacion"
     * con Remito -> :DetalleRemito actualizarDocumentacion(fhActual, responsable). Privado, como en el
     * diagrama de clases (-). Recibe además los tres arreglos de documentación marcada (A3/A4/A5).
     */
    private void actualizarDocumentacion(LocalDateTime fhActual, Empleado responsable, int[] nrosNoRecibida,
            int[] nrosRechazada, int[] nrosParaRedirigir) {
        for (DetalleRemito detalle : detalleRemito) {
            detalle.actualizarDocumentacion(fhActual, responsable, nrosNoRecibida, nrosRechazada, nrosParaRedirigir);
        }
    }
}
