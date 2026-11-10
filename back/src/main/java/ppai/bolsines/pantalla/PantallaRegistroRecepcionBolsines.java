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
 * «boundary» del CU 28. Cada mensaje del Encargado de Bolsines es un endpoint con el mismo nombre
 * que en el diagrama de secuencia. Los mensajes mostrarX() que le manda el gestor guardan lo que
 * hay que mostrar, y el endpoint se lo devuelve al front (React), que lo dibuja.
 * Es una sola pantalla para un solo EB: los endpoints son synchronized para que dos pedidos
 * simultáneos (por ejemplo, un doble clic o React en modo desarrollo) no mezclen lo que se muestra.
 */
@RestController
@RequestMapping("/api/registro-recepcion-bolsin")
@RequiredArgsConstructor
public class PantallaRegistroRecepcionBolsines {

    private final GestorRegistroRecepcionBolsines gestor;

    // Lo que la pantalla muestra (gridCM, cmbBolsin, cmbRemitoDocumentacion, gridOpcionesRecepcion, ...).
    private String nombreCM;
    private List<BolsinParaSeleccion> bolsines;
    private List<RemitoConDocumentacion> remitos;
    private List<String> opciones;
    private String mensaje;
    private boolean seleccionarDocumentacion;

    @PostMapping("/opcRegistrarRecepcionBolsin")
    public synchronized OpcRegistrarRecepcionResponse opcRegistrarRecepcionBolsin() {
        habilitarPantalla();
        gestor.opcRegistrarRecepcionBolsin();
        return new OpcRegistrarRecepcionResponse(nombreCM, bolsines);
    }

    public void habilitarPantalla() {
        nombreCM = null;
        bolsines = List.of();
        remitos = List.of();
        opciones = List.of();
        mensaje = null;
        seleccionarDocumentacion = false;
    }

    public void mostrarCM(String nombre) {
        nombreCM = nombre;
    }

    public void mostrarBolsinesEnviadosParaSeleccion(int[] nroPrecinto, String[] origen) {
        bolsines = IntStream.range(0, nroPrecinto.length)
                .mapToObj(i -> new BolsinParaSeleccion(nroPrecinto[i], origen[i]))
                .toList();
    }

    @PostMapping("/tomarSeleccionBolsin")
    public synchronized SeleccionBolsinResponse tomarSeleccionBolsin(@RequestBody SeleccionBolsinRequest request) {
        gestor.tomarSeleccionBolsin(request.nroPrecinto());
        return new SeleccionBolsinResponse(remitos, opciones);
    }

    public void mostrarNumRemitosyDocumentacionAsociada(int[] nrosRemito, String[][] documentacion) {
        remitos = RemitoConDocumentacion.agrupar(nrosRemito, documentacion);
    }

    public void mostrarOpcionesRecepcionParaSeleccion(String[] opciones) {
        this.opciones = List.of(opciones);
    }

    @PostMapping("/tomarSeleccionOpcionRecepcion")
    public synchronized SeleccionOpcionResponse tomarSeleccionOpcionRecepcion(@Valid @RequestBody SeleccionOpcionRequest request) {
        gestor.tomarSeleccionOpcionRecepcion(request.opcion());
        return new SeleccionOpcionResponse(mensaje, seleccionarDocumentacion);
    }

    public void solicitarConfirmacionRegistro() {
        mensaje = "¿Confirma el registro de la recepción del bolsín?";
        seleccionarDocumentacion = false;
    }

    /** A3: pide que el EB marque la documentación que no llegó (extensión del diagrama). */
    public void solicitarSeleccionDocumentacionNoRecibida() {
        mensaje = "Seleccione la documentación que no se recibió.";
        seleccionarDocumentacion = true;
    }

    /** A4: pide que el EB marque la documentación que no corresponde a su CM (extensión del diagrama). */
    public void solicitarSeleccionDocumentacionRechazada() {
        mensaje = "Seleccione la documentación que no corresponde a esta comisión médica.";
        seleccionarDocumentacion = true;
    }

    /** A5: pide que el EB marque la documentación que va a otra área de la CMC (extensión del diagrama). */
    public void solicitarSeleccionDocumentacionParaRedirigir() {
        mensaje = "Seleccione la documentación que se debe redirigir a otra área de la Comisión Médica Central.";
        seleccionarDocumentacion = true;
    }

    /** A3, A4 y A5: mensaje nuevo del EB a la pantalla (extensión del diagrama). */
    @PostMapping("/tomarSeleccionDocumentacion")
    public synchronized MensajeResponse tomarSeleccionDocumentacion(@Valid @RequestBody SeleccionDocumentacionRequest request) {
        gestor.tomarSeleccionDocumentacion(request.nrosDocumentacion());
        return new MensajeResponse(mensaje);
    }

    @PostMapping("/tomarConfirmacionRegistro")
    public synchronized MensajeResponse tomarConfirmacionRegistro(@RequestBody ConfirmacionRequest request) {
        gestor.tomarConfirmacionRegistro(request.confirmacion());
        return new MensajeResponse(mensaje);
    }

    public void mostrarEjecucionExitosa() {
        mensaje = "Se registró la recepción del bolsín y se notificó a cada comisión médica de origen.";
    }

    /** A6: el EB no confirma el registro (flujo alternativo, no está en el diagrama). */
    public void mostrarRegistroCancelado() {
        mensaje = "Registro cancelado: el bolsín no se modificó.";
    }
}
