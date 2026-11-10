package ppai.bolsines.modelo.bolsin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import ppai.bolsines.modelo.remito.EstadoRemito;
import ppai.bolsines.modelo.remito.Remito;
import ppai.bolsines.modelo.usuario.Empleado;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bolsin {

    @Id
    private int numero;
    private LocalDate fecha;
    private int numeroPrecinto;
    private int peso;

    @ManyToOne
    @JoinColumn(name = "origen_codigo")
    private ComisionMedica origen;

    @ManyToOne
    @JoinColumn(name = "destino_codigo")
    private ComisionMedica destino;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "bolsin_numero")
    private List<Remito> remito = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "bolsin_numero")
    private List<CambioEstadoBolsin> cambioEstado = new ArrayList<>();

    /** true si el estado actual del bolsín es Enviado. */
    public boolean buscarBolsinesEnviados() {
        for (CambioEstadoBolsin ce : cambioEstado) {
            if (ce.sosActual()) {
                return ce.sosEnviado();
            }
        }
        return false;
    }

    public boolean esCmDestino(ComisionMedica cm) {
        return destino.equals(cm);
    }

    /** A5: la opción de redirigir a otra área sólo corresponde si el destino es la CMC. */
    public boolean esDestinoCentral() {
        return destino.esCentral();
    }

    public int getNumPrecinto() {
        return numeroPrecinto;
    }

    public ComisionMedica getOrigen() {
        return origen;
    }

    /** Números de los remitos del bolsín: el bolsín le pide getNumero() a cada remito. */
    public int[] buscarRemitos() {
        return remito.stream().mapToInt(Remito::getNumero).toArray();
    }

    /** Filas [nroRemito, nroDocumentacion, asunto, tipo, areaCMCDestino] de todos los remitos del bolsín. */
    public String[][] buscarDocumentacion() {
        return remito.stream()
                .flatMap(r -> Arrays.stream(r.buscarDatosDocumentacion()))
                .toArray(String[][]::new);
    }

    /** CU 29: comisiones médicas de origen de los remitos del bolsín; cada una recibe su notificación. */
    public List<ComisionMedica> buscarOrigenesDocumentacion() {
        return remito.stream().map(Remito::getOrigen).distinct().toList();
    }

    /** CU 29: filas [nroRemito, nroDocumentacion, asunto, estado] sólo de los remitos que envió esa CM. */
    public String[][] buscarSituacionDocumentacion(ComisionMedica cmOrigen) {
        return remito.stream()
                .filter(r -> r.getOrigen().equals(cmOrigen))
                .flatMap(r -> Arrays.stream(r.buscarSituacionDocumentacion()))
                .toArray(String[][]::new);
    }

    /**
     * Primero actualiza remitos y documentación; recién después cierra el cambio de estado
     * actual del bolsín (setFechaHoraFin al CE que sosActual) y crea el nuevo (corrección del tutor).
     * A3, A4 y A5 agregan al final el estado parcial del remito y la documentación que el EB marcó como
     * no recibida (A3), rechazada (A4) o para redirigir (A5); vacías en el flujo descripto. El bolsín
     * queda RecibidoEnCMDestino en todos los casos.
     */
    public void actualizarEstado(LocalDateTime fhActual, EstadoBolsin estadoBolsin, EstadoRemito estadoRemito,
            Empleado responsable, EstadoRemito estadoRemitoParcial, int[] nrosNoRecibida, int[] nrosRechazada,
            int[] nrosParaRedirigir) {
        actualizarRemitos(fhActual, estadoRemito, responsable, estadoRemitoParcial, nrosNoRecibida, nrosRechazada,
                nrosParaRedirigir);
        for (CambioEstadoBolsin ce : cambioEstado) {
            if (ce.sosActual()) {
                ce.setFechaHoraFin(fhActual);
            }
        }
        cambioEstado.add(crearCambioEstado(fhActual, estadoBolsin, responsable));
    }

    private void actualizarRemitos(LocalDateTime fhActual, EstadoRemito estadoRemito, Empleado responsable,
            EstadoRemito estadoRemitoParcial, int[] nrosNoRecibida, int[] nrosRechazada, int[] nrosParaRedirigir) {
        for (Remito r : remito) {
            r.actualizarEstado(estadoRemito, fhActual, responsable, estadoRemitoParcial, nrosNoRecibida, nrosRechazada,
                    nrosParaRedirigir);
        }
    }

    private CambioEstadoBolsin crearCambioEstado(LocalDateTime fhInicio, EstadoBolsin e, Empleado responsable) {
        return new CambioEstadoBolsin(fhInicio, e, responsable);
    }
}
