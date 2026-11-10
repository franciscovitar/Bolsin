package ppai.bolsines.modelo.documentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import ppai.bolsines.modelo.usuario.Empleado;

/** Pruebas del patrón State sobre Documentación, sin base de datos. */
class EstadoDocumentacionTest {

    private static final LocalDateTime ENVIO = LocalDateTime.of(2026, 11, 6, 8, 30);
    private static final LocalDateTime RECEPCION = LocalDateTime.of(2026, 11, 9, 10, 0);

    private final Empleado encargado = mock(Empleado.class);

    /** Documentación cuyo estado actual es el indicado, con su cambio de estado abierto. */
    private Documentacion documentacionEn(EstadoDocumentacion estado) {
        Documentacion doc = new Documentacion(1, "Expediente 1/2026", LocalDate.of(2026, 11, 3),
                new TipoDocumento(1, "Expediente", "Expediente de trámite"));
        doc.setEstado(estado);
        doc.agregarCE(new CambioEstadoDocumentacion(ENVIO, estado, encargado));
        return doc;
    }

    private void verificarTransicion(Documentacion doc, Class<? extends EstadoDocumentacion> esperado) {
        assertInstanceOf(esperado, doc.getEstadoActual());
        assertEquals(2, doc.getCambioEstado().size());
        CambioEstadoDocumentacion anterior = doc.getCambioEstado().get(0);
        CambioEstadoDocumentacion nuevo = doc.getCambioEstado().get(1);
        assertFalse(anterior.sosActual(), "el cambio de estado anterior queda cerrado");
        assertTrue(nuevo.sosActual(), "el nuevo cambio de estado es el actual");
        assertInstanceOf(esperado, nuevo.getEstado());
    }

    @Test
    void enBolsinEnviadoRecibirYAceptarPasaARecibidaYAceptada() {
        Documentacion doc = documentacionEn(new EnBolsinEnviado());
        doc.recibirYAceptar(RECEPCION, encargado);
        verificarTransicion(doc, RecibidaYAceptada.class);
    }

    @Test
    void enBolsinEnviadoRecibirYRechazarPasaARecibidaYRechazada() {
        Documentacion doc = documentacionEn(new EnBolsinEnviado());
        doc.recibirYRechazar(RECEPCION, encargado);
        verificarTransicion(doc, RecibidaYRechazada.class);
    }

    @Test
    void enBolsinEnviadoRegistrarNoRecibidaPasaANoRecibida() {
        Documentacion doc = documentacionEn(new EnBolsinEnviado());
        doc.registrarNoRecibida(RECEPCION, encargado);
        verificarTransicion(doc, NoRecibida.class);
    }

    @Test
    void enBolsinEnviadoRedirigirPasaAParaRedirigir() {
        Documentacion doc = documentacionEn(new EnBolsinEnviado());
        doc.redirigir(RECEPCION, encargado);
        verificarTransicion(doc, ParaRedirigir.class);
    }

    @Test
    void paraRedirigirElAreaAceptaLaDocumentacion() {
        Documentacion doc = documentacionEn(new ParaRedirigir());
        doc.recibirYAceptar(RECEPCION, encargado);
        verificarTransicion(doc, RecibidaYAceptada.class);
    }

    @Test
    void recibidaYRechazadaRedirigirPasaAParaRedirigirConSuHistorial() {
        Empleado responsableRedireccion = mock(Empleado.class);
        Documentacion doc = documentacionEn(new RecibidaYRechazada());
        doc.redirigir(RECEPCION, responsableRedireccion);
        verificarTransicion(doc, ParaRedirigir.class);
        CambioEstadoDocumentacion anterior = doc.getCambioEstado().get(0);
        CambioEstadoDocumentacion nuevo = doc.getCambioEstado().get(1);
        assertInstanceOf(RecibidaYRechazada.class, anterior.getEstado());
        assertEquals(ENVIO, anterior.getFechaHoraInicio());
        assertEquals(RECEPCION, anterior.getFechaHoraFin(), "se cierra en la fecha de la transición");
        assertSame(encargado, anterior.getResponsableCE());
        assertEquals(RECEPCION, nuevo.getFechaHoraInicio());
        assertSame(responsableRedireccion, nuevo.getResponsableCE(), "registra al empleado responsable");
    }

    @Test
    void unEstadoSinEsaTransicionLaRechazaYNoCambiaNada() {
        Documentacion doc = documentacionEn(new Registrada());
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> doc.recibirYAceptar(RECEPCION, encargado));
        assertTrue(error.getMessage().contains("Registrada"));
        assertInstanceOf(Registrada.class, doc.getEstadoActual());
        assertEquals(1, doc.getCambioEstado().size());
        assertTrue(doc.getCambioEstado().get(0).sosActual());
    }

    @Test
    void paraRedirigirNoSePuedeVolverARedirigir() {
        Documentacion doc = documentacionEn(new ParaRedirigir());
        assertThrows(IllegalStateException.class, () -> doc.redirigir(RECEPCION, encargado));
    }

    @Test
    void elConvertidorGuardaElNombreYReconstruyeElEstadoConcreto() {
        EstadoDocumentacionConverter convertidor = new EstadoDocumentacionConverter();
        assertEquals("EnBolsinEnviado", convertidor.convertToDatabaseColumn(new EnBolsinEnviado()));
        assertInstanceOf(EnBolsinEnviado.class, convertidor.convertToEntityAttribute("EnBolsinEnviado"));
        assertThrows(IllegalArgumentException.class, () -> convertidor.convertToEntityAttribute("Inventado"));
    }
}
