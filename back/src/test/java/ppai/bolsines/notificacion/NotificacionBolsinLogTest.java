package ppai.bolsines.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** El texto del correo que deja la implementación de log (CU 29), con la situación de cada documentación. */
class NotificacionBolsinLogTest {

    @Test
    void redactaElBolsinLaCMDestinoYLaSituacionDeCadaDocumentacion() {
        NotificacionRecepcionBolsin notificacion = NotificacionRecepcionBolsin.de("cm.central@bolsines.test",
                "Comisión Médica Central", 5001, "Comisión Médica Córdoba", new String[][] {
                        { "1001", "1", "Expediente 1/2026 - Pérez, Juan", "RecibidaYRechazada" },
                        { "1001", "2", "Dictamen incapacidad - Gómez, Ana", "RecibidaYAceptada" } });

        assertEquals("""
                CU 29 - Correo a cm.central@bolsines.test (Comisión Médica Central): el bolsín con precinto 5001 \
                fue recibido en Comisión Médica Córdoba. Situación de la documentación enviada:
                  - Remito 1001, documentación 1 "Expediente 1/2026 - Pérez, Juan": RecibidaYRechazada
                  - Remito 1001, documentación 2 "Dictamen incapacidad - Gómez, Ana": RecibidaYAceptada""",
                new NotificacionBolsinLog().redactar(notificacion));
    }
}
