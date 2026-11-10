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

    public int getNumero() {
        return numero;
    }

    /** Una fila [nroRemito, nroDocumentacion, asunto, tipo, areaCMCDestino] por cada documentación del remito. */
    public String[][] buscarDatosDocumentacion() {
        List<String[]> filas = new ArrayList<>();
        for (DetalleRemito detalle : detalleRemito) {
            String[] datos = detalle.buscarDatosDocumentacion();
            filas.add(new String[] { String.valueOf(numero), datos[0], datos[1], datos[2], datos[3] });
        }
        return filas.toArray(String[][]::new);
    }

    /** CU 29: la CM que generó el remito es la que envió su documentación. */
    public ComisionMedica getOrigen() {
        return origen;
    }

    /** CU 29: una fila [nroRemito, nroDocumentacion, asunto, estado] por cada documentación del remito. */
    public String[][] buscarSituacionDocumentacion() {
        List<String[]> filas = new ArrayList<>();
        for (DetalleRemito detalle : detalleRemito) {
            String[] datos = detalle.buscarSituacionDocumentacion();
            filas.add(new String[] { String.valueOf(numero), datos[0], datos[1], datos[2] });
        }
        return filas.toArray(String[][]::new);
    }

    /**
     * Si alguna documentación del remito no se aceptó (no recibida o rechazada), queda recibido y
     * aceptado parcial; si no, recibido y aceptado. A3: regla de la ERS. A4 y A5 (la redirigida no
     * cuenta como no aceptada): decisiones provisionales del grupo, la ERS no lo dice.
     * En el flujo descripto los arreglos están vacíos.
     */
    public void actualizarEstado(EstadoRemito estadoRemito, LocalDateTime fhActual, Empleado responsable,
            EstadoRemito estadoRemitoParcial, int[] nrosNoRecibida, int[] nrosRechazada, int[] nrosParaRedirigir) {
        setEstado(tieneDocumentacionNoAceptada(nrosNoRecibida, nrosRechazada) ? estadoRemitoParcial : estadoRemito);
        actualizarDocumentacion(fhActual, responsable, nrosNoRecibida, nrosRechazada, nrosParaRedirigir);
    }

    public void setEstado(EstadoRemito e) {
        this.estado = e;
    }

    private boolean tieneDocumentacionNoAceptada(int[] nrosNoRecibida, int[] nrosRechazada) {
        return detalleRemito.stream().anyMatch(detalle -> detalle.esNoAceptada(nrosNoRecibida, nrosRechazada));
    }

    private void actualizarDocumentacion(LocalDateTime fhActual, Empleado responsable, int[] nrosNoRecibida,
            int[] nrosRechazada, int[] nrosParaRedirigir) {
        for (DetalleRemito detalle : detalleRemito) {
            detalle.actualizarDocumentacion(fhActual, responsable, nrosNoRecibida, nrosRechazada, nrosParaRedirigir);
        }
    }
}
