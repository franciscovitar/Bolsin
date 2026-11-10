package ppai.bolsines.pantalla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import ppai.bolsines.dto.ConfirmacionRequest;
import ppai.bolsines.dto.OpcRegistrarRecepcionResponse;
import ppai.bolsines.dto.SeleccionBolsinRequest;
import ppai.bolsines.dto.SeleccionBolsinResponse;
import ppai.bolsines.dto.SeleccionDocumentacionRequest;
import ppai.bolsines.dto.SeleccionOpcionRequest;
import ppai.bolsines.dto.SeleccionOpcionResponse;
import ppai.bolsines.notificacion.InterfazNotificacionBolsin;
import ppai.bolsines.notificacion.NotificacionRecepcionBolsin;
import ppai.bolsines.notificacion.NotificacionRecepcionBolsin.SituacionDocumentacion;

/**
 * CU 28 de punta a punta: Pantalla -> Gestor -> entidades -> base. Usa una H2 en memoria cargada
 * con data.sql (no toca la base en archivo de la demo) y revisa lo persistido con SQL. Cada prueba
 * arranca con la base recién cargada (@DirtiesContext), así no dependen del orden. Las pruebas de A5
 * agregan el escenario aislado de la CMC (datos-cmc.sql), que no forma parte de la demo.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:cu28;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class RegistroRecepcionBolsinIntegracionTest {

    private static final String OPCION_1 = "El contenido del bolsín es igual al registrado";
    private static final String OPCION_2 = "No se recibe toda la documentación asociada a los remitos";
    private static final String OPCION_3 = "Existe documentación que no corresponde al destino";
    private static final String OPCION_4 = "La documentación se debe redirigir a otra área";
    private static final int LAURA = 1; // empleado de la sesión activa en data.sql
    private static final int ANA = 3; // empleada de la sesión activa en datos-cmc.sql
    private static final String CORDOBA = "Comisión Médica Córdoba";
    private static final String CENTRAL = "Comisión Médica Central";

    private final PantallaRegistroRecepcionBolsines pantalla;
    private final JdbcTemplate jdbc;

    /** CU 29: se espía la implementación real para verificar que el «include» se invoca. */
    @MockitoSpyBean
    private InterfazNotificacionBolsin notificacion;

    @Autowired
    RegistroRecepcionBolsinIntegracionTest(PantallaRegistroRecepcionBolsines pantalla, JdbcTemplate jdbc) {
        this.pantalla = pantalla;
        this.jdbc = jdbc;
    }

    @Test
    void flujoDescriptoTodoQuedaRecibidoYAceptado() {
        SeleccionOpcionResponse respuesta = elegirOpcion(5002, OPCION_1);
        assertFalse(respuesta.seleccionarDocumentacion());

        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        assertEquals("RecibidaYAceptada", estadoDocumentacion(4));
        assertEquals("RecibidaYAceptada", estadoDocumentacion(5));
        assertEquals("RecibidoYAceptado", estadoRemito(1003));
        assertEquals("RecibidoEnCMDestino", estadoActualBolsin(102));
        verificarHistorial(5, "RecibidaYAceptada", 102);
        verificarNotificacion(unicaNotificacion(), "cm.rosario@bolsines.test", "Comisión Médica Rosario", 5002, CORDOBA,
                situacion(1003, 4, "Expediente 77/2026 - Díaz, Carla", "RecibidaYAceptada"),
                situacion(1003, 5, "Nota de elevación", "RecibidaYAceptada"));
    }

    @Test
    void a3LaDocumentacionMarcadaQuedaNoRecibidaYSuRemitoParcial() {
        SeleccionBolsinResponse contenido = seleccionarBolsin(5001);
        assertEquals(List.of(1, 2, 3), contenido.remitos().stream()
                .flatMap(r -> r.documentacion().stream()).map(d -> d.nroDocumentacion()).toList());
        SeleccionOpcionResponse respuesta = pantalla.tomarSeleccionOpcionRecepcion(new SeleccionOpcionRequest(OPCION_2));
        assertTrue(respuesta.seleccionarDocumentacion());

        marcar(2);
        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        assertEquals("RecibidaYAceptada", estadoDocumentacion(1));
        assertEquals("NoRecibida", estadoDocumentacion(2));
        assertEquals("RecibidaYAceptada", estadoDocumentacion(3));
        assertEquals("RecibidoYAceptadoParcial", estadoRemito(1001), "le faltó la documentación 2");
        assertEquals("RecibidoYAceptado", estadoRemito(1002), "llegó completo");
        assertEquals("RecibidoEnCMDestino", estadoActualBolsin(101));
        verificarHistorial(2, "NoRecibida", 101);
        verificarHistorial(1, "RecibidaYAceptada", 101);
        verificarNotificacion(unicaNotificacion(), "cm.central@bolsines.test", "Comisión Médica Central", 5001, CORDOBA,
                situacion(1001, 1, "Expediente 1/2026 - Pérez, Juan", "RecibidaYAceptada"),
                situacion(1001, 2, "Dictamen incapacidad - Gómez, Ana", "NoRecibida"),
                situacion(1002, 3, "Historia clínica - Ríos, Pedro", "RecibidaYAceptada"));
    }

    @Test
    void a4LaDocumentacionMarcadaQuedaRechazadaYElRestoDelMismoRemitoAceptada() {
        SeleccionOpcionResponse respuesta = elegirOpcion(5001, OPCION_3);
        assertTrue(respuesta.seleccionarDocumentacion());
        assertEquals("Seleccione la documentación que no corresponde a esta comisión médica.", respuesta.mensaje());

        marcar(1);
        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        assertEquals("RecibidaYRechazada", estadoDocumentacion(1), "marcada: no corresponde al destino");
        assertEquals("RecibidaYAceptada", estadoDocumentacion(2), "mismo remito 1001, no marcada");
        assertEquals("RecibidaYAceptada", estadoDocumentacion(3));
        assertEquals("RecibidoYAceptadoParcial", estadoRemito(1001), "decisión provisional del grupo");
        assertEquals("RecibidoYAceptado", estadoRemito(1002));
        assertEquals("RecibidoEnCMDestino", estadoActualBolsin(101));
        verificarHistorial(1, "RecibidaYRechazada", 101);
        verificarHistorial(2, "RecibidaYAceptada", 101);
        verificarNotificacion(unicaNotificacion(), "cm.central@bolsines.test", "Comisión Médica Central", 5001, CORDOBA,
                situacion(1001, 1, "Expediente 1/2026 - Pérez, Juan", "RecibidaYRechazada"),
                situacion(1001, 2, "Dictamen incapacidad - Gómez, Ana", "RecibidaYAceptada"),
                situacion(1002, 3, "Historia clínica - Ríos, Pedro", "RecibidaYAceptada"));
    }

    @Test
    void a6DespuesDeMarcarNoConfirmaYNadaCambia() {
        assertTrue(elegirOpcion(5005, OPCION_3).seleccionarDocumentacion());
        marcar(8);

        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(false));

        assertEquals("EnBolsinEnviado", estadoDocumentacion(8));
        assertEquals("EnBolsinEnviado", estadoDocumentacion(9));
        assertEquals("Enviado", estadoRemito(1006));
        assertEquals("Enviado", estadoActualBolsin(105));
        verify(notificacion, never()).notificarRecepcionBolsin(any());
    }

    @Test
    void a3ValidaLaSeleccionYElOrdenDeLosPasos() {
        elegirOpcion(5005, OPCION_2);
        assertThrows(IllegalStateException.class,
                () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)), "falta marcar");
        assertThrows(IllegalArgumentException.class, () -> marcar(1), "la documentación 1 es de otro bolsín");
        assertThrows(IllegalArgumentException.class, () -> marcar());

        elegirOpcion(5005, OPCION_1);
        assertThrows(IllegalStateException.class, () -> marcar(8), "con la opción 1 no se marca documentación");
        assertEquals("EnBolsinEnviado", estadoDocumentacion(8));
    }

    @Test
    void a4UnaSeleccionInvalidaNoGuardaNadaYCambiarDeOpcionDescartaLaMarca() {
        elegirOpcion(5005, OPCION_3);
        assertThrows(IllegalStateException.class,
                () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)), "falta marcar");
        assertThrows(IllegalArgumentException.class, () -> marcar(8, 1), "la 1 es de otro bolsín");
        assertThrows(IllegalStateException.class,
                () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)),
                "la 8 tampoco quedó marcada: la selección inválida no se guarda a medias");
        assertThrows(IllegalArgumentException.class, () -> marcar());
        assertEquals("EnBolsinEnviado", estadoDocumentacion(8));

        marcar(8);
        pantalla.tomarSeleccionOpcionRecepcion(new SeleccionOpcionRequest(OPCION_1)); // el EB cambia de opción
        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        assertEquals("RecibidaYAceptada", estadoDocumentacion(8), "la marca de A4 se descartó");
        assertEquals("RecibidaYAceptada", estadoDocumentacion(9));
        assertEquals("RecibidoYAceptado", estadoRemito(1006));
    }

    @Test
    @Sql("/datos-cmc.sql")
    void a5ConDestinoCmcLaDocumentacionMarcadaQuedaParaRedirigir() {
        OpcRegistrarRecepcionResponse inicio = pantalla.opcRegistrarRecepcionBolsin();
        assertEquals(CENTRAL, inicio.nombreCM());
        assertEquals(List.of(5006), inicio.bolsines().stream().map(b -> b.nroPrecinto()).toList());
        SeleccionBolsinResponse contenido = pantalla.tomarSeleccionBolsin(new SeleccionBolsinRequest(5006));
        assertEquals("Área de Dictámenes", contenido.remitos().get(0).documentacion().get(0).areaCMCDestino());
        SeleccionOpcionResponse respuesta = pantalla.tomarSeleccionOpcionRecepcion(new SeleccionOpcionRequest(OPCION_4));
        assertTrue(respuesta.seleccionarDocumentacion());

        marcar(10);
        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        assertEquals("ParaRedirigir", estadoDocumentacion(10), "marcada: va a otra área de la CMC");
        assertEquals("RecibidaYAceptada", estadoDocumentacion(11), "mismo remito 1007, no marcada");
        assertEquals("RecibidaYAceptada", estadoDocumentacion(12));
        assertEquals("RecibidoYAceptado", estadoRemito(1007), "decisión provisional: redirigir no deja el remito parcial");
        assertEquals("RecibidoYAceptado", estadoRemito(1008));
        assertEquals("RecibidoEnCMDestino", estadoActualBolsin(106));
        verificarHistorial(10, "ParaRedirigir", 106, ANA);
        verificarHistorial(11, "RecibidaYAceptada", 106, ANA);
        verificarNotificacion(unicaNotificacion(), "cm.cordoba@bolsines.test", CORDOBA, 5006, CENTRAL,
                situacion(1007, 10, "Expediente 101/2026 - Sosa, Lía", "ParaRedirigir"),
                situacion(1007, 11, "Dictamen apelación - Mena, Raúl", "RecibidaYAceptada"),
                situacion(1008, 12, "Nota de elevación a la CMC", "RecibidaYAceptada"));
    }

    @Test
    void a5ConDestinoDistintoDeCmcNoSePuedeElegir() {
        seleccionarBolsin(5001); // Laura, EB de Córdoba
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> pantalla.tomarSeleccionOpcionRecepcion(new SeleccionOpcionRequest(OPCION_4)));
        assertTrue(error.getMessage().contains("Central"));
        assertThrows(IllegalStateException.class, () -> marcar(1), "la opción no quedó elegida");
        assertThrows(IllegalStateException.class,
                () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)), "no hay opción elegida");
        assertEquals("EnBolsinEnviado", estadoDocumentacion(1));
        assertEquals("Enviado", estadoActualBolsin(101));
        verify(notificacion, never()).notificarRecepcionBolsin(any());
    }

    @Test
    @Sql("/datos-cmc.sql")
    void a5ValidaLaSeleccionSinGuardarNadaAMedias() {
        elegirOpcion(5006, OPCION_4);
        assertThrows(IllegalArgumentException.class, () -> marcar(11), "la 11 no tiene área de la CMC registrada");
        assertThrows(IllegalArgumentException.class, () -> marcar(1), "la 1 es de otro bolsín");
        assertThrows(IllegalArgumentException.class, () -> marcar(10, 11), "una inválida invalida toda la selección");
        assertThrows(IllegalArgumentException.class, () -> marcar());
        assertThrows(IllegalStateException.class,
                () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)), "no quedó nada marcado");
        assertEquals("EnBolsinEnviado", estadoDocumentacion(10));
        assertEquals("Enviado", estadoActualBolsin(106));
    }

    @Test
    @Sql("/datos-cmc.sql")
    void a5DespuesDeMarcarNoConfirmaYNadaCambia() {
        elegirOpcion(5006, OPCION_4);
        marcar(10, 12);

        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(false));

        assertEquals("EnBolsinEnviado", estadoDocumentacion(10));
        assertEquals("EnBolsinEnviado", estadoDocumentacion(12));
        assertEquals("Enviado", estadoRemito(1007));
        assertEquals("Enviado", estadoActualBolsin(106));
        assertEquals(4, jdbc.queryForObject(
                "SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_numero = 10", Integer.class));
        verify(notificacion, never()).notificarRecepcionBolsin(any());
    }

    @Test
    @Sql("/datos-varios-origenes.sql")
    void cu29ConVariasCMOrigenCadaUnaRecibeSoloSuDocumentacion() {
        elegirOpcion(5001, OPCION_3);
        marcar(13); // la documentación de Rosario no corresponde a Córdoba
        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        List<NotificacionRecepcionBolsin> enviadas = notificaciones(2);
        verificarNotificacion(paraEmail(enviadas, "cm.central@bolsines.test"), "cm.central@bolsines.test",
                "Comisión Médica Central", 5001, CORDOBA,
                situacion(1001, 1, "Expediente 1/2026 - Pérez, Juan", "RecibidaYAceptada"),
                situacion(1001, 2, "Dictamen incapacidad - Gómez, Ana", "RecibidaYAceptada"),
                situacion(1002, 3, "Historia clínica - Ríos, Pedro", "RecibidaYAceptada"));
        verificarNotificacion(paraEmail(enviadas, "cm.rosario@bolsines.test"), "cm.rosario@bolsines.test",
                "Comisión Médica Rosario", 5001, CORDOBA,
                situacion(1009, 13, "Dictamen - Ferrer, Inés", "RecibidaYRechazada"));
        assertEquals("RecibidoYAceptadoParcial", estadoRemito(1009));
        assertEquals("RecibidoYAceptado", estadoRemito(1001));
    }

    @Test
    void siFallaLaNotificacionNoSeRegistraLaRecepcion() {
        doThrow(new IllegalStateException("El servidor de correo no responde"))
                .when(notificacion).notificarRecepcionBolsin(any());
        elegirOpcion(5002, OPCION_1);

        assertThrows(IllegalStateException.class, () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)));

        assertEquals("EnBolsinEnviado", estadoDocumentacion(4), "la transacción se revirtió");
        assertEquals("EnBolsinEnviado", estadoDocumentacion(5));
        assertEquals("Enviado", estadoRemito(1003));
        assertEquals("Enviado", estadoActualBolsin(102));
        assertEquals(4, jdbc.queryForObject(
                "SELECT COUNT(*) FROM cambio_estado_documentacion WHERE documentacion_numero = 4", Integer.class));
        assertThrows(IllegalStateException.class, () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)),
                "el registro en curso se descartó: no se puede reintentar sobre objetos a medio actualizar");
    }

    /**
     * O1: cuando se emite la notificación, los cambios ya están escritos en la base (dentro de la
     * transacción todavía abierta). Así, un error de SQL aparece antes de notificar, nunca después.
     */
    @Test
    void cuandoSeNotificaLosCambiosYaEstanEscritosEnLaBase() {
        List<String> estadoAlNotificar = new ArrayList<>();
        doAnswer(invocacion -> {
            estadoAlNotificar.add(estadoDocumentacion(4)); // misma transacción y conexión
            estadoAlNotificar.add(estadoRemito(1003));
            return invocacion.callRealMethod();
        }).when(notificacion).notificarRecepcionBolsin(any());
        elegirOpcion(5002, OPCION_1);

        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        assertEquals(List.of("RecibidaYAceptada", "RecibidoYAceptado"), estadoAlNotificar);
    }

    /** Elegir otro bolsín invalida la opción y la documentación marcadas para el bolsín anterior. */
    @Test
    void cambiarDeBolsinInvalidaLaOpcionYLaSeleccionAnteriores() {
        elegirOpcion(5001, OPCION_2);
        marcar(2);
        pantalla.tomarSeleccionBolsin(new SeleccionBolsinRequest(5005)); // sin elegir una opción nueva

        assertThrows(IllegalStateException.class, // 409 en la API
                () -> pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true)));

        for (int nro : new int[] { 1, 2, 3, 8, 9 }) {
            assertEquals("EnBolsinEnviado", estadoDocumentacion(nro));
        }
        assertEquals("Enviado", estadoRemito(1001));
        assertEquals("Enviado", estadoRemito(1006));
        assertEquals("Enviado", estadoActualBolsin(101));
        assertEquals("Enviado", estadoActualBolsin(105));
        verify(notificacion, never()).notificarRecepcionBolsin(any());

        pantalla.tomarSeleccionOpcionRecepcion(new SeleccionOpcionRequest(OPCION_1)); // opción válida para el 5005
        pantalla.tomarConfirmacionRegistro(new ConfirmacionRequest(true));

        assertEquals("RecibidaYAceptada", estadoDocumentacion(8));
        assertEquals("RecibidaYAceptada", estadoDocumentacion(9));
        assertEquals("RecibidoYAceptado", estadoRemito(1006));
        assertEquals("RecibidoEnCMDestino", estadoActualBolsin(105));
        assertEquals("Enviado", estadoActualBolsin(101), "el 5001 no se tocó");
        verificarNotificacion(unicaNotificacion(), "cm.mendoza@bolsines.test", "Comisión Médica Mendoza", 5005, CORDOBA,
                situacion(1006, 8, "Historia clínica - Paz, Eva", "RecibidaYAceptada"),
                situacion(1006, 9, "Expediente 12/2026 - Rey, Hugo", "RecibidaYAceptada"));
    }

    private NotificacionRecepcionBolsin unicaNotificacion() {
        return notificaciones(1).get(0);
    }

    private List<NotificacionRecepcionBolsin> notificaciones(int cantidad) {
        ArgumentCaptor<NotificacionRecepcionBolsin> captor = ArgumentCaptor.forClass(NotificacionRecepcionBolsin.class);
        verify(notificacion, times(cantidad)).notificarRecepcionBolsin(captor.capture());
        return captor.getAllValues();
    }

    private static NotificacionRecepcionBolsin paraEmail(List<NotificacionRecepcionBolsin> enviadas, String email) {
        return enviadas.stream().filter(n -> n.emailCMOrigen().equals(email)).findFirst().orElseThrow();
    }

    private static SituacionDocumentacion situacion(int nroRemito, int nroDocumentacion, String asunto, String estado) {
        return new SituacionDocumentacion(nroRemito, nroDocumentacion, asunto, estado);
    }

    /** Compara todo el contenido; la documentación se ordena por número porque el orden de los remitos no importa. */
    private static void verificarNotificacion(NotificacionRecepcionBolsin n, String email, String nombreCMOrigen,
            int nroPrecinto, String nombreCMDestino, SituacionDocumentacion... esperada) {
        Comparator<SituacionDocumentacion> porNumero = Comparator.comparingInt(SituacionDocumentacion::nroDocumentacion);
        assertEquals(email, n.emailCMOrigen());
        assertEquals(nombreCMOrigen, n.nombreCMOrigen());
        assertEquals(nroPrecinto, n.nroPrecinto());
        assertEquals(nombreCMDestino, n.nombreCMDestino());
        assertEquals(Stream.of(esperada).sorted(porNumero).toList(), n.documentacion().stream().sorted(porNumero).toList());
    }

    private SeleccionBolsinResponse seleccionarBolsin(int nroPrecinto) {
        pantalla.opcRegistrarRecepcionBolsin();
        return pantalla.tomarSeleccionBolsin(new SeleccionBolsinRequest(nroPrecinto));
    }

    private SeleccionOpcionResponse elegirOpcion(int nroPrecinto, String opcion) {
        seleccionarBolsin(nroPrecinto);
        return pantalla.tomarSeleccionOpcionRecepcion(new SeleccionOpcionRequest(opcion));
    }

    private void marcar(int... nrosDocumentacion) {
        pantalla.tomarSeleccionDocumentacion(new SeleccionDocumentacionRequest(nrosDocumentacion));
    }

    private void verificarHistorial(int nroDocumentacion, String estadoNuevo, int nroBolsin) {
        verificarHistorial(nroDocumentacion, estadoNuevo, nroBolsin, LAURA);
    }

    /**
     * El EnBolsinEnviado se cerró en el mismo instante en que empezó el nuevo cambio de estado, que es
     * el actual, tiene al EB logueado como responsable y la misma fecha y hora que el nuevo estado
     * del bolsín.
     */
    private void verificarHistorial(int nroDocumentacion, String estadoNuevo, int nroBolsin, int responsable) {
        assertEquals(estadoNuevo, jdbc.queryForObject("SELECT estado FROM cambio_estado_documentacion "
                + "WHERE documentacion_numero = ? AND fecha_hora_fin IS NULL", String.class, nroDocumentacion));
        LocalDateTime inicioNuevo = jdbc.queryForObject("SELECT fecha_hora_inicio FROM cambio_estado_documentacion "
                + "WHERE documentacion_numero = ? AND fecha_hora_fin IS NULL", LocalDateTime.class, nroDocumentacion);
        LocalDateTime finAnterior = jdbc.queryForObject("SELECT fecha_hora_fin FROM cambio_estado_documentacion "
                + "WHERE documentacion_numero = ? AND estado = 'EnBolsinEnviado'", LocalDateTime.class, nroDocumentacion);
        LocalDateTime inicioBolsin = jdbc.queryForObject("SELECT fecha_hora_inicio FROM cambio_estado_bolsin "
                + "WHERE bolsin_numero = ? AND fecha_hora_fin IS NULL", LocalDateTime.class, nroBolsin);
        assertNotNull(finAnterior);
        assertEquals(finAnterior, inicioNuevo);
        assertEquals(inicioBolsin, inicioNuevo);
        assertEquals(responsable, jdbc.queryForObject("SELECT responsable_id FROM cambio_estado_documentacion "
                + "WHERE documentacion_numero = ? AND fecha_hora_fin IS NULL", Integer.class, nroDocumentacion));
    }

    private String estadoDocumentacion(int numero) {
        return jdbc.queryForObject("SELECT estado_actual FROM documentacion WHERE numero = ?", String.class, numero);
    }

    private String estadoRemito(int numero) {
        return jdbc.queryForObject("SELECT e.nombre FROM remito r JOIN estado_remito e ON e.id = r.estado_id "
                + "WHERE r.numero = ?", String.class, numero);
    }

    private String estadoActualBolsin(int numero) {
        return jdbc.queryForObject("SELECT e.nombre FROM cambio_estado_bolsin c JOIN estado_bolsin e ON e.id = c.estado_id "
                + "WHERE c.bolsin_numero = ? AND c.fecha_hora_fin IS NULL", String.class, numero);
    }
}
