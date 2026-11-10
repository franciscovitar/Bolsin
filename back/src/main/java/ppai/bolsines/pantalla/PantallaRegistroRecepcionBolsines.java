package ppai.bolsines.pantalla;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ppai.bolsines.dto.ConfirmacionRequest;
import ppai.bolsines.dto.MensajeResponse;
import ppai.bolsines.dto.OpcRegistrarRecepcionResponse;
import ppai.bolsines.dto.OpcRegistrarRecepcionResponse.BolsinParaSeleccion;
import ppai.bolsines.dto.SeleccionBolsinRequest;
import ppai.bolsines.dto.SeleccionBolsinResponse;
import ppai.bolsines.dto.SeleccionBolsinResponse.RemitoConDocumentacion;
import ppai.bolsines.dto.SeleccionDocumentacionRequest;
import ppai.bolsines.dto.SeleccionOpcionRequest;
import ppai.bolsines.dto.SeleccionOpcionResponse;
import ppai.bolsines.gestor.GestorRegistroRecepcionBolsines;

/**
 * «boundary» PantallaRegistroRecepcionBolsines del diagrama de clases y lifeline
 * ":PantallaRegistroRecepcionBolsines" del diagrama de secuencia.
 *
 * Responsabilidad: es la frontera con el actor Encargado de Bolsines (EB). Recibe sus mensajes, se los
 * pasa al gestor y guarda lo que el gestor le pide mostrar. No decide nada del negocio.
 *
 * Cómo se implementa:
 * - Cada mensaje "EB -> Pantalla" de la secuencia es un endpoint POST con el mismo nombre
 *   (opcRegistrarRecepcionBolsin, tomarSeleccionBolsin, tomarSeleccionOpcionRecepcion,
 *   tomarConfirmacionRegistro).
 * - Cada mensaje "Gestor -> Pantalla" (mostrarCM, mostrarBolsinesEnviadosParaSeleccion, ...) es un
 *   método público que guarda lo que hay que mostrar; el endpoint lo devuelve al front (React).
 * - Diferencia con el diagrama de clases: allí los métodos de la pantalla devuelven void; acá los
 *   endpoints devuelven un record de dto con lo mostrado, porque la pantalla real se dibuja en el
 *   navegador.
 * - Es una sola pantalla para un solo EB: los endpoints son synchronized para que dos pedidos
 *   simultáneos (por ejemplo, un doble clic o React en modo desarrollo) no mezclen lo que se muestra.
 */
@RestController
@RequestMapping("/api/registro-recepcion-bolsin")
@RequiredArgsConstructor
public class PantallaRegistroRecepcionBolsines {

    private final GestorRegistroRecepcionBolsines gestor;

    // Lo que la pantalla muestra. En el diagrama de clases son controles (gridCM, cmbBolsín,
    // cmbRemitoDocumentación, gridOpcionesRecepción, txtConfirmación, lblEjecucionExitosa, ...); acá se
    // guardan sus datos y los controles visuales están en el front.
    private String nombreCM;
    private List<BolsinParaSeleccion> bolsines;
    private List<RemitoConDocumentacion> remitos;
    private List<String> opciones;
    private String mensaje;
    private boolean seleccionarDocumentacion; // extensión A3/A4/A5: indica al front que sigue marcar documentación

    /** Secuencia: EB -> Pantalla opcRegistrarRecepcionBolsin(); luego self habilitarPantalla() y Pantalla -> Gestor. */
    @PostMapping("/opcRegistrarRecepcionBolsin")
    public synchronized OpcRegistrarRecepcionResponse opcRegistrarRecepcionBolsin() {
        habilitarPantalla();
        gestor.opcRegistrarRecepcionBolsin();
        return new OpcRegistrarRecepcionResponse(nombreCM, bolsines);
    }

    /** Secuencia: Pantalla self habilitarPantalla(). Limpia lo mostrado en un registro anterior. */
    public void habilitarPantalla() {
        nombreCM = null;
        bolsines = List.of();
        remitos = List.of();
        opciones = List.of();
        mensaje = null;
        seleccionarDocumentacion = false;
    }

    /** Secuencia: Gestor -> Pantalla mostrarCM(nombre: String). Paso 2 del CU. */
    public void mostrarCM(String nombre) {
        nombreCM = nombre;
    }

    /**
     * Secuencia: Gestor -> Pantalla mostrarBolsinesEnviadosParaSeleccion(nroPrecinto: int[], origen: String[]).
     * Paso 4 del CU. Une los dos arreglos (misma posición = mismo bolsín) en la lista que filtra el front
     * (A1 y A2 se resuelven en el front con esa lista).
     */
    public void mostrarBolsinesEnviadosParaSeleccion(int[] nroPrecinto, String[] origen) {
        bolsines = IntStream.range(0, nroPrecinto.length)
                .mapToObj(i -> new BolsinParaSeleccion(nroPrecinto[i], origen[i]))
                .toList();
    }

    /** Secuencia: EB -> Pantalla tomarSeleccionBolsin(nroPrecinto: int) y Pantalla -> Gestor con el mismo nombre. Paso 5. */
    @PostMapping("/tomarSeleccionBolsin")
    public synchronized SeleccionBolsinResponse tomarSeleccionBolsin(@RequestBody SeleccionBolsinRequest request) {
        gestor.tomarSeleccionBolsin(request.nroPrecinto());
        return new SeleccionBolsinResponse(remitos, opciones);
    }

    /**
     * Secuencia: Gestor -> Pantalla mostrarNumRemitosyDocumentacionAsociada(nrosRemito: int[],
     * documentacion: String[][]). Paso 6 (en el diagrama de clases figura como
     * mostrarNumRemitosYDocumentaciónAsociada). Extensión: cada fila trae también el número de
     * documentación (para marcarla en A3/A4/A5) y el área de la CMC (A5).
     */
    public void mostrarNumRemitosyDocumentacionAsociada(int[] nrosRemito, String[][] documentacion) {
        remitos = RemitoConDocumentacion.agrupar(nrosRemito, documentacion);
    }

    /** Secuencia: Gestor -> Pantalla mostrarOpcionesRecepcionParaSeleccion(opciones: String[]). Paso 7. */
    public void mostrarOpcionesRecepcionParaSeleccion(String[] opciones) {
        this.opciones = List.of(opciones);
    }

    /** Secuencia: EB -> Pantalla tomarSeleccionOpcionRecepcion(opcion: String) y Pantalla -> Gestor. Paso 8. */
    @PostMapping("/tomarSeleccionOpcionRecepcion")
    public synchronized SeleccionOpcionResponse tomarSeleccionOpcionRecepcion(@Valid @RequestBody SeleccionOpcionRequest request) {
        gestor.tomarSeleccionOpcionRecepcion(request.opcion());
        return new SeleccionOpcionResponse(mensaje, seleccionarDocumentacion);
    }

    /** Secuencia: Gestor -> Pantalla solicitarConfirmacionRegistro(). Paso 9. */
    public void solicitarConfirmacionRegistro() {
        mensaje = "¿Confirma el registro de la recepción del bolsín?";
        seleccionarDocumentacion = false;
    }

    /** Extensión A3 (no está en la secuencia): pide que el EB marque la documentación que no llegó. */
    public void solicitarSeleccionDocumentacionNoRecibida() {
        mensaje = "Seleccione la documentación que no se recibió.";
        seleccionarDocumentacion = true;
    }

    /** Extensión A4 (no está en la secuencia): pide que el EB marque la documentación que no corresponde a su CM. */
    public void solicitarSeleccionDocumentacionRechazada() {
        mensaje = "Seleccione la documentación que no corresponde a esta comisión médica.";
        seleccionarDocumentacion = true;
    }

    /** Extensión A5 (no está en la secuencia): pide que el EB marque la documentación que va a otra área de la CMC. */
    public void solicitarSeleccionDocumentacionParaRedirigir() {
        mensaje = "Seleccione la documentación que se debe redirigir a otra área de la Comisión Médica Central.";
        seleccionarDocumentacion = true;
    }

    /**
     * Extensión A3, A4 y A5 (no está en la secuencia): EB -> Pantalla tomarSeleccionDocumentacion(nrosDocumentacion)
     * y Pantalla -> Gestor con el mismo nombre. Va entre la elección de la opción y la confirmación.
     */
    @PostMapping("/tomarSeleccionDocumentacion")
    public synchronized MensajeResponse tomarSeleccionDocumentacion(@Valid @RequestBody SeleccionDocumentacionRequest request) {
        gestor.tomarSeleccionDocumentacion(request.nrosDocumentacion());
        return new MensajeResponse(mensaje);
    }

    /**
     * Secuencia: EB -> Pantalla tomarConfirmacionRegistro(confirmacion: boolean) y Pantalla -> Gestor
     * (en la flecha de la secuencia está escrito "tomarConfirmaionRegistro", errata). Paso 10; con
     * false es el flujo A6.
     */
    @PostMapping("/tomarConfirmacionRegistro")
    public synchronized MensajeResponse tomarConfirmacionRegistro(@RequestBody ConfirmacionRequest request) {
        gestor.tomarConfirmacionRegistro(request.confirmacion());
        return new MensajeResponse(mensaje);
    }

    /** Secuencia: Gestor -> Pantalla mostrarEjecucionExitosa(). Paso 12. */
    public void mostrarEjecucionExitosa() {
        mensaje = "Se registró la recepción del bolsín y se notificó a cada comisión médica de origen.";
    }

    /** Extensión A6 (no está en la secuencia): el EB no confirma; se informa que no se modificó nada. */
    public void mostrarRegistroCancelado() {
        mensaje = "Registro cancelado: el bolsín no se modificó.";
    }
}
