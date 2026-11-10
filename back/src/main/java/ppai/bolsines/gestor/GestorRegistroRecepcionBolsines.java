package ppai.bolsines.gestor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ppai.bolsines.modelo.bolsin.Bolsin;
import ppai.bolsines.modelo.bolsin.EstadoBolsin;
import ppai.bolsines.modelo.comisionmedica.ComisionMedica;
import ppai.bolsines.modelo.remito.EstadoRemito;
import ppai.bolsines.modelo.usuario.Empleado;
import ppai.bolsines.modelo.usuario.Usuario;
import ppai.bolsines.notificacion.InterfazNotificacionBolsin;
import ppai.bolsines.notificacion.NotificacionRecepcionBolsin;
import ppai.bolsines.pantalla.PantallaRegistroRecepcionBolsines;
import ppai.bolsines.repositorio.BolsinRepositorio;
import ppai.bolsines.repositorio.EmpleadoRepositorio;
import ppai.bolsines.repositorio.EstadoBolsinRepositorio;
import ppai.bolsines.repositorio.EstadoRemitoRepositorio;
import ppai.bolsines.repositorio.SesionRepositorio;

/**
 * Gestor del CU 28 («control» en el diagrama de clases).
 * Coordina los pasos de la recepción y recuerda lo que selecciona el encargado.
 * Las reglas sobre bolsines, remitos y documentación las resuelven las entidades (Experto).
 * En la secuencia es el objeto :GestorRegistroRecepcionBolsines.
 *
 * Los métodos adicionales para A3, A4, A5 y CU 29 no figuran en la secuencia original.
 * Las diferencias de firmas y visibilidad están en docs/diferencias_codigo_diagramas.md.
 */
@Service
@RequiredArgsConstructor
public class GestorRegistroRecepcionBolsines {

    // Opciones del paso 7 del CU (ERS v1.2). Lo que devuelve getOpcionesRecepcion().
    private static final String OPCION_CONTENIDO_IGUAL = "El contenido del bolsín es igual al registrado";
    private static final String OPCION_NO_SE_RECIBE_TODA = "No se recibe toda la documentación asociada a los remitos";
    private static final String OPCION_NO_CORRESPONDE = "Existe documentación que no corresponde al destino";
    private static final String OPCION_REDIRIGIR = "La documentación se debe redirigir a otra área";
    private static final String[] OPCIONES_RECEPCION = {
            OPCION_CONTENIDO_IGUAL,
            OPCION_NO_SE_RECIBE_TODA,
            OPCION_NO_CORRESPONDE,
            OPCION_REDIRIGIR };
    /** Opciones en las que el EB marca documentación antes de confirmar (A3, A4 y A5). */
    private static final List<String> OPCIONES_CON_SELECCION = List.of(OPCION_NO_SE_RECIBE_TODA,
            OPCION_NO_CORRESPONDE, OPCION_REDIRIGIR);

    // La pantalla conoce al gestor y el gestor a la pantalla: @Lazy corta el ciclo al crear los beans.
    @Lazy
    private final PantallaRegistroRecepcionBolsines pantalla;
    private final Clock reloj;
    private final SesionRepositorio sesionRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final BolsinRepositorio bolsinRepositorio;
    private final EstadoBolsinRepositorio estadoBolsinRepositorio;
    private final EstadoRemitoRepositorio estadoRemitoRepositorio;
    private final InterfazNotificacionBolsin interfazNotificacion;

    // Atributos del gestor. Del diagrama de clases: encargado, bolsines, opcRecepcion, fechaHoraActual,
    // estadoRecibidoEnCMDestino y estadoRecibidoYAceptado. En el diagrama, nroPrecintoCM, origenCM y
    // nroRemito son valores sueltos y asuntoDocumentación y tipoDocumentación son datos de una sola
    // documentación; acá se guardan como listas (nrosPrecinto, origenes, nrosRemito, documentacion).
    // cmUsuario (resultado de buscarCMUsuario) y bolsinSeleccionado (el "selec:Bolsin" de la secuencia)
    // no figuran como atributos en el diagrama.
    private Empleado encargado;
    private ComisionMedica cmUsuario;
    private List<Bolsin> bolsines;
    private int[] nrosPrecinto;
    private String[] origenes;
    private Bolsin bolsinSeleccionado;
    private int[] nrosRemito;
    private String[][] documentacion;
    private String opcRecepcion;
    private LocalDateTime fechaHoraActual;
    private EstadoBolsin estadoRecibidoEnCMDestino;
    private EstadoRemito estadoRecibidoYAceptado;
    // A3, A4 y A5 (extensión del diagrama): documentación que el EB marcó como no recibida,
    // rechazada o para redirigir a otra área de la CMC.
    private int[] nrosDocumentacionNoRecibida;
    private int[] nrosDocumentacionRechazada;
    private int[] nrosDocumentacionParaRedirigir;
    private EstadoRemito estadoRecibidoYAceptadoParcial; // extensión A3/A4

    /**
     * En la secuencia: Pantalla -> Gestor opcRegistrarRecepcionBolsin(). Pasos 1 a 4 del CU:
     * buscarUsuarioLogueado(), buscarCMUsuario(), mostrarCM(nombre), buscarBolsinesEnviados() y
     * mostrarBolsinesEnviadosParaSeleccion(...).
     */
    @Transactional(readOnly = true)
    public void opcRegistrarRecepcionBolsin() {
        finCU(); // descarta un registro anterior que el EB canceló sin terminar (Obs. 1 del CU)
        buscarUsuarioLogueado();
        buscarCMUsuario();
        pantalla.mostrarCM(cmUsuario.getNombre());
        buscarBolsinesEnviados();
        pantalla.mostrarBolsinesEnviadosParaSeleccion(nrosPrecinto, origenes);
    }

    /**
     * En la secuencia: Pantalla -> Gestor tomarSeleccionBolsin(nroPrecinto: int). Pasos 5 a 7: buscarRemitos(),
     * buscarDocumentacion(), mostrarNumRemitosyDocumentacionAsociada(...), getOpcionesRecepcion() y
     * mostrarOpcionesRecepcionParaSeleccion(...). El bolsín elegido es el "selec:Bolsin" de la secuencia.
     */
    public void tomarSeleccionBolsin(int nroPrecinto) {
        if (bolsines == null) {
            throw new IllegalStateException("Primero hay que elegir la opción de registrar la recepción de un bolsín");
        }
        bolsinSeleccionado = bolsines.stream()
                .filter(b -> b.getNumPrecinto() == nroPrecinto)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un bolsín enviado con el número de precinto " + nroPrecinto));
        // Al elegir otro bolsín se invalidan la opción y la documentación marcadas antes: correspondían al
        // bolsín anterior. Así no se puede confirmar este bolsín sin haberle elegido una opción (paso 8).
        opcRecepcion = null;
        nrosDocumentacionNoRecibida = null;
        nrosDocumentacionRechazada = null;
        nrosDocumentacionParaRedirigir = null;
        buscarRemitos();
        buscarDocumentacion();
        pantalla.mostrarNumRemitosyDocumentacionAsociada(nrosRemito, documentacion);
        pantalla.mostrarOpcionesRecepcionParaSeleccion(getOpcionesRecepcion());
    }

    /**
     * En la secuencia: Pantalla -> Gestor tomarSeleccionOpcionRecepcion(opcion: String) y luego
     * Gestor -> Pantalla solicitarConfirmacionRegistro(). Pasos 8 y 9 (la secuencia modela la opción 1).
     * Extensiones: con las opciones 2, 3 y 4 (A3, A4 y A5), antes de confirmar se pide marcar
     * documentación con solicitarSeleccionDocumentacion...(). La opción 4 sólo vale si el destino es la
     * CMC: se le pregunta al bolsín (Experto), no se decide acá. Las comparaciones son sobre la opción
     * que eligió el EB, nunca sobre el nombre de un estado.
     */
    public void tomarSeleccionOpcionRecepcion(String opcion) {
        if (bolsinSeleccionado == null) {
            throw new IllegalStateException("Primero hay que seleccionar un bolsín");
        }
        if (!List.of(OPCIONES_RECEPCION).contains(opcion)) {
            throw new IllegalArgumentException("Opción de recepción desconocida: " + opcion);
        }
        if (OPCION_REDIRIGIR.equals(opcion) && !bolsinSeleccionado.esDestinoCentral()) { // A5 (ERS)
            throw new IllegalStateException("La opción \"" + OPCION_REDIRIGIR
                    + "\" sólo corresponde cuando la comisión médica destino es la Central");
        }
        opcRecepcion = opcion;
        nrosDocumentacionNoRecibida = new int[0];
        nrosDocumentacionRechazada = new int[0];
        nrosDocumentacionParaRedirigir = new int[0];
        if (OPCION_NO_SE_RECIBE_TODA.equals(opcion)) { // A3: antes de confirmar, el EB marca lo que no llegó
            pantalla.solicitarSeleccionDocumentacionNoRecibida();
        } else if (OPCION_NO_CORRESPONDE.equals(opcion)) { // A4: marca lo que no corresponde a su CM
            pantalla.solicitarSeleccionDocumentacionRechazada();
        } else if (OPCION_REDIRIGIR.equals(opcion)) { // A5: marca lo que va a otra área de la CMC
            pantalla.solicitarSeleccionDocumentacionParaRedirigir();
        } else {
            pantalla.solicitarConfirmacionRegistro();
        }
    }

    /**
     * Extensión A3, A4 y A5 (no está en la secuencia): Pantalla -> Gestor tomarSeleccionDocumentacion(...).
     * Primero se valida toda la selección: si algún número no es del bolsín (o, en A5, no tiene un
     * área de la CMC registrada en su detalle de remito), no se guarda nada.
     */
    public void tomarSeleccionDocumentacion(int[] nrosDocumentacion) {
        if (opcRecepcion == null || !OPCIONES_CON_SELECCION.contains(opcRecepcion)) {
            throw new IllegalStateException("La opción de recepción elegida no pide seleccionar documentación");
        }
        if (nrosDocumentacion == null || nrosDocumentacion.length == 0) {
            throw new IllegalArgumentException("Seleccione al menos una documentación");
        }
        for (int nro : nrosDocumentacion) {
            String[] fila = Arrays.stream(documentacion)
                    .filter(f -> f[1].equals(String.valueOf(nro)))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La documentación " + nro + " no pertenece al bolsín seleccionado"));
            if (OPCION_REDIRIGIR.equals(opcRecepcion) && fila[4] == null) {
                throw new IllegalArgumentException("La documentación " + nro
                        + " no tiene registrada el área de la Comisión Médica Central a la que va");
            }
        }
        int[] seleccion = Arrays.stream(nrosDocumentacion).distinct().toArray();
        if (OPCION_NO_SE_RECIBE_TODA.equals(opcRecepcion)) {
            nrosDocumentacionNoRecibida = seleccion;
        } else if (OPCION_NO_CORRESPONDE.equals(opcRecepcion)) {
            nrosDocumentacionRechazada = seleccion;
        } else {
            nrosDocumentacionParaRedirigir = seleccion;
        }
        pantalla.solicitarConfirmacionRegistro();
    }

    /**
     * En la secuencia: Pantalla -> Gestor tomarConfirmacionRegistro(confirmacion: boolean) (escrito
     * "tomarConfirmaionRegistro" en el diagrama). Pasos 10 a 12: actualizarBolsin(), llamarCasoUso29(),
     * mostrarEjecucionExitosa() y FinCU() (finCU() en el código).
     * Diferencia: en la secuencia mostrarEjecucionExitosa() aparece antes del «include»; acá el CU 29 va
     * primero para no mostrar un éxito si la notificación falla. Con confirmacion = false es el flujo A6
     * (extensión: mostrarRegistroCancelado()). Todo corre en una transacción: si algo falla, no queda nada
     * guardado.
     */
    @Transactional
    public void tomarConfirmacionRegistro(boolean confirmacion) {
        if (opcRecepcion == null) {
            throw new IllegalStateException("Primero hay que elegir una opción de recepción");
        }
        if (OPCIONES_CON_SELECCION.contains(opcRecepcion) && nrosDocumentacionNoRecibida.length
                + nrosDocumentacionRechazada.length + nrosDocumentacionParaRedirigir.length == 0) {
            throw new IllegalStateException("Primero hay que seleccionar la documentación");
        }
        if (!confirmacion) { // A6: el EB no confirma
            pantalla.mostrarRegistroCancelado();
            finCU();
            return;
        }
        try {
            actualizarBolsin();
            llamarCasoUso29();
        } catch (RuntimeException e) {
            finCU(); // la transacción se revierte; se descartan los objetos que quedaron a medio actualizar
            throw e;
        }
        pantalla.mostrarEjecucionExitosa();
        finCU();
    }

    /**
     * En la secuencia: mensaje a sí mismo buscarUsuarioLogueado(). Gestor -> Activa:Sesion getUsuario(): Usuario (la Sesion
     * a su vez le envía getUsuario() a :Usuario) y Gestor -> :Empleado *esTuUsuario(u): el "*" es este for.
     * Obtener la sesión activa (la que no tiene fechaHoraFin) con el repositorio es diseño de persistencia.
     */
    private void buscarUsuarioLogueado() {
        Usuario usuario = sesionRepositorio.findFirstByFechaHoraFinIsNull()
                .orElseThrow(() -> new IllegalStateException("No hay una sesión activa"))
                .getUsuario();
        encargado = null;
        for (Empleado empleado : empleadoRepositorio.findAll()) {
            if (empleado.esTuUsuario(usuario)) {
                encargado = empleado;
            }
        }
        if (encargado == null) {
            throw new IllegalStateException("El usuario logueado no corresponde a ningún empleado");
        }
    }

    /**
     * En la secuencia: mensaje a sí mismo buscarCMUsuario() y Gestor -> Logueado:Empleado obtenerCM(): ComisionMedica.
     * Diferencia: en el diagrama el getNombre() a :ComisionMedica lo envía Empleado; acá lo envía el
     * gestor (en opcRegistrarRecepcionBolsin), porque necesita el objeto CM y también su nombre.
     */
    private void buscarCMUsuario() {
        cmUsuario = encargado.obtenerCM();
    }

    /**
     * En la secuencia: mensaje a sí mismo buscarBolsinesEnviados() y el loop "EstadosBolsin [mientras haya bolsines]":
     * Gestor -> :Bolsin buscarBolsinesEnviados(): boolean, esCmDestino(cm), getNumPrecinto() y
     * getOrigen() (el getNombre() lo envía el gestor; ver buscarCMUsuario). En el diagrama de clases
     * Bolsin.buscarBolsinesEnviados recibe cmUsuario; acá se sigue a la secuencia (sin parámetro).
     */
    private void buscarBolsinesEnviados() {
        bolsines = new ArrayList<>();
        for (Bolsin bolsin : bolsinRepositorio.findAll()) {
            if (bolsin.buscarBolsinesEnviados() && bolsin.esCmDestino(cmUsuario)) {
                bolsines.add(bolsin);
            }
        }
        nrosPrecinto = bolsines.stream().mapToInt(Bolsin::getNumPrecinto).toArray();
        origenes = bolsines.stream().map(b -> b.getOrigen().getNombre()).toArray(String[]::new);
    }

    /** En la secuencia: mensaje a sí mismo buscarRemitos() y Gestor -> selec:Bolsin buscarRemitos(). */
    private void buscarRemitos() {
        nrosRemito = bolsinSeleccionado.buscarRemitos();
    }

    /** En la secuencia: mensaje a sí mismo buscarDocumentacion() y Gestor -> selec:Bolsin buscarDocumentacion(): String[][]. */
    private void buscarDocumentacion() {
        documentacion = bolsinSeleccionado.buscarDocumentacion();
    }

    /** En la secuencia: mensaje a sí mismo getOpcionesRecepcion(). En el diagrama de clases devuelve void; acá String[]. */
    private String[] getOpcionesRecepcion() {
        return OPCIONES_RECEPCION.clone();
    }

    /** En la secuencia: mensaje a sí mismo getFechaHoraActual(): datetime. La hora sale del bean Clock. */
    private LocalDateTime getFechaHoraActual() {
        return LocalDateTime.now(reloj);
    }

    /**
     * En la secuencia: mensaje a sí mismo actualizarBolsin(). Loop "Estado Bolsin" con *esRecibidoEnCMDestino() a
     * :EstadoBolsin y loop de estados de remito con *esRecibidoyAceptado() a :EstadoRemito (punteros
     * a los estados nuevos), getFechaHoraActual() y Gestor -> selec:Bolsin actualizarEstado(fhActual,
     * estadoBolsin, estadoRemito, responsable).
     * Diferencia por A3, A4 y A5: actualizarEstado recibe al final 4 parámetros más (estado parcial
     * del remito y la documentación marcada), y el loop de remito también busca
     * esRecibidoYAceptadoParcial(). En el flujo descripto los arreglos van vacíos.
     */
    private void actualizarBolsin() {
        for (EstadoBolsin estado : estadoBolsinRepositorio.findAll()) {
            if (estado.esRecibidoEnCMDestino()) {
                estadoRecibidoEnCMDestino = estado;
            }
        }
        for (EstadoRemito estado : estadoRemitoRepositorio.findAll()) {
            if (estado.esRecibidoyAceptado()) {
                estadoRecibidoYAceptado = estado;
            }
            if (estado.esRecibidoYAceptadoParcial()) {
                estadoRecibidoYAceptadoParcial = estado;
            }
        }
        fechaHoraActual = getFechaHoraActual();
        bolsinSeleccionado.actualizarEstado(fechaHoraActual, estadoRecibidoEnCMDestino, estadoRecibidoYAceptado,
                encargado, estadoRecibidoYAceptadoParcial, nrosDocumentacionNoRecibida, nrosDocumentacionRechazada,
                nrosDocumentacionParaRedirigir);
        // Persistencia (no es un mensaje del diagrama). saveAndFlush escribe todo el SQL ahora, antes de
        // llamarCasoUso29(): un error de base revierte la transacción sin haber notificado. Queda el riesgo
        // de que falle el commit posterior (no se puede "desnotificar"); ver docs/decisiones_para_la_tutora.md.
        bolsinRepositorio.saveAndFlush(bolsinSeleccionado);
    }

    /**
     * En la secuencia: mensaje a sí mismo llamarCasoUso29() y el «include» a "29. Notificar recepción de Bolsin".
     * Diferencia (CU 29): la secuencia muestra un solo «include»; acá se notifica una vez por cada CM de
     * origen de los remitos, cada una sólo con la situación de su documentación (CU 28 paso 12: "a cada
     * CM de origen que envió documentación"). El bolsín responde orígenes y situación (Experto); el gestor
     * sólo arma el mensaje y depende de la interfaz, no de cómo se envía (Bajo Acoplamiento).
     */
    private void llamarCasoUso29() {
        for (ComisionMedica cmOrigen : bolsinSeleccionado.buscarOrigenesDocumentacion()) {
            interfazNotificacion.notificarRecepcionBolsin(NotificacionRecepcionBolsin.de(cmOrigen.getEmail(),
                    cmOrigen.getNombre(), bolsinSeleccionado.getNumPrecinto(), cmUsuario.getNombre(),
                    bolsinSeleccionado.buscarSituacionDocumentacion(cmOrigen)));
        }
    }

    /** En la secuencia: mensaje a sí mismo FinCU() (finCU() en el diagrama de clases). Olvida todo lo que eligió el EB. */
    private void finCU() {
        bolsines = null;
        nrosPrecinto = null;
        origenes = null;
        bolsinSeleccionado = null;
        nrosRemito = null;
        documentacion = null;
        opcRecepcion = null;
        nrosDocumentacionNoRecibida = null;
        nrosDocumentacionRechazada = null;
        nrosDocumentacionParaRedirigir = null;
        fechaHoraActual = null;
    }
}
