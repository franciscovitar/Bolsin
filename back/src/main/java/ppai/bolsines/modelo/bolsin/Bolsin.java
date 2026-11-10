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

/**
 * Bolsín del diagrama de clases. En la secuencia aparece como :Bolsin cuando se
 * buscan los enviados y como selec:Bolsin cuando el encargado elige uno.
 * Por Experto (GRASP), conoce su origen, destino, remitos e historial y puede
 * coordinar la actualización de los remitos al registrar la recepción.
 *
 * Para A3, A4 y A5 se agregaron parámetros a actualizarEstado().
 * Las diferencias con el diagrama original están documentadas.
 */
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

    /**
     * En la secuencia: Gestor -> :Bolsin buscarBolsinesEnviados(): boolean; el bolsín envía *sosActual() a
     * :CambioEstadoBolsin y sosEnviado() al Actual, que se lo pregunta a :EstadoBolsin.
     * Diferencia: en el diagrama de clases recibe (cmUsuario: ComisionMedica); acá, como en la secuencia,
     * no recibe nada y la CM se pregunta aparte con esCmDestino.
     */
    public boolean buscarBolsinesEnviados() {
        for (CambioEstadoBolsin ce : cambioEstado) {
            if (ce.sosActual()) {
                return ce.sosEnviado();
            }
        }
        return false;
    }

    /** En la secuencia: Gestor -> :Bolsin esCmDestino(cm: ComisionMedica): boolean (esCMDestino en el diagrama de clases). */
    public boolean esCmDestino(ComisionMedica cm) {
        return destino.equals(cm);
    }

    /**
     * Extensión A5 (no está en el diagrama): la opción de redirigir a otra área sólo corresponde si el
     * destino es la CMC. Experto: el bolsín le pregunta a su destino (ComisionMedica.esCentral()).
     */
    public boolean esDestinoCentral() {
        return destino.esCentral();
    }

    /** En la secuencia: Gestor -> :Bolsin getNumPrecinto(): int. */
    public int getNumPrecinto() {
        return numeroPrecinto;
    }

    /**
     * En la secuencia: Gestor -> :Bolsin getOrigen(): ComisionMedica. En el diagrama, Bolsín le envía getNombre()
     * a :ComisionMedica; acá devuelve la CM y el getNombre() lo envía el gestor.
     */
    public ComisionMedica getOrigen() {
        return origen;
    }

    /**
     * En la secuencia: Gestor -> selec:Bolsin buscarRemitos() y Bolsin -> :Remito *getNumero(): int.
     * Diferencia: el diagrama de clases dice que devuelve Remito[]; acá devuelve int[] (los números),
     * porque el *getNumero() lo envía el bolsín y así el gestor no se acopla a Remito.
     */
    public int[] buscarRemitos() {
        return remito.stream().mapToInt(Remito::getNumero).toArray();
    }

    /**
     * En la secuencia: Gestor -> selec:Bolsin buscarDocumentacion(): String[][]; loop "Remitos del Bolsin" con
     * Bolsin -> :Remito buscarDatosDocumentacion(). Cada fila es [nroRemito, nroDocumentacion, asunto,
     * tipo, areaCMCDestino]; nroDocumentacion y areaCMCDestino son extensiones de A3/A4/A5.
     */
    public String[][] buscarDocumentacion() {
        return remito.stream()
                .flatMap(r -> Arrays.stream(r.buscarDatosDocumentacion()))
                .toArray(String[][]::new);
    }

    /**
     * Extensión CU 29 (no está en el diagrama): CM de origen de los remitos del bolsín (asociación origen de
     * Remito); cada una recibe su propia notificación. No se supone que todas sean el origen del bolsín.
     */
    public List<ComisionMedica> buscarOrigenesDocumentacion() {
        return remito.stream().map(Remito::getOrigen).distinct().toList();
    }

    /**
     * Extensión CU 29 (no está en el diagrama): filas [nroRemito, nroDocumentacion, asunto, estado] sólo de
     * los remitos que envió esa CM, para no mezclar documentación de otra CM de origen.
     */
    public String[][] buscarSituacionDocumentacion(ComisionMedica cmOrigen) {
        return remito.stream()
                .filter(r -> r.getOrigen().equals(cmOrigen))
                .flatMap(r -> Arrays.stream(r.buscarSituacionDocumentacion()))
                .toArray(String[][]::new);
    }

    /**
     * En la secuencia: Gestor -> selec:Bolsin actualizarEstado(fhActual: DateTime, estadoBolsin: EstadoBolsin,
     * estadoRemito: EstadoRemito, responsable: Empleado). Orden de la secuencia (corrección del tutor):
     * primero mensaje a sí mismo actualizarRemitos(...) (remitos y documentación); recién después setFechaHoraFin al
     * Actual:CambioEstadoBolsin (buscado con sosActual()) y mensaje a sí mismo crearCambioEstado(), que crea el
     * Nuevo:CambioEstadoBolsin. El bolsín queda RecibidoEnCMDestino en todos los flujos.
     * Diferencia por A3, A4 y A5: recibe al final estadoRemitoParcial, nrosNoRecibida, nrosRechazada y
     * nrosParaRedirigir (8 parámetros contra 4 del diagrama). En el flujo descripto los arreglos van vacíos.
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

    /**
     * En la secuencia: mensaje a sí mismo actualizarRemitos(fhActual, estadoRemito, responsable) y el loop "Actualizar Estado
     * Remitos" con Bolsin -> :Remito actualizarEstado(estadoRemito, fhActual, responsable). Privado, como
     * en el diagrama de clases (-). Mismos 4 parámetros agregados que actualizarEstado.
     */
    private void actualizarRemitos(LocalDateTime fhActual, EstadoRemito estadoRemito, Empleado responsable,
            EstadoRemito estadoRemitoParcial, int[] nrosNoRecibida, int[] nrosRechazada, int[] nrosParaRedirigir) {
        for (Remito r : remito) {
            r.actualizarEstado(estadoRemito, fhActual, responsable, estadoRemitoParcial, nrosNoRecibida, nrosRechazada,
                    nrosParaRedirigir);
        }
    }

    /**
     * En la secuencia: mensaje a sí mismo crearCambioEstado() y new(fhInicio, e, usuarioResponsable) -> Nuevo:CambioEstadoBolsin.
     * Diferencia: en el diagrama de clases es público y sin parámetros; acá es privado y recibe los datos
     * que necesita el new(...), para no guardarlos como atributos temporales. Por Creador (GRASP), el bolsín
     * contiene su historial, así que crea sus cambios de estado.
     */
    private CambioEstadoBolsin crearCambioEstado(LocalDateTime fhInicio, EstadoBolsin e, Empleado responsable) {
        return new CambioEstadoBolsin(fhInicio, e, responsable);
    }
}
