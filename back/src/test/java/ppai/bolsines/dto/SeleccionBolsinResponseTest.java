package ppai.bolsines.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ppai.bolsines.dto.SeleccionBolsinResponse.DatosDocumentacion;
import ppai.bolsines.dto.SeleccionBolsinResponse.RemitoConDocumentacion;

/**
 * La pantalla agrupa las filas por remito y conserva el número que identifica a cada documentación
 * y el área de la CMC de su detalle de remito.
 */
class SeleccionBolsinResponseTest {

    @Test
    void agrupaPorRemitoConElNumeroYElAreaDeCadaDocumentacion() {
        String[][] filas = {
                { "1001", "1", "Expediente 1", "Expediente", null },
                { "1001", "2", "Expediente 1", "Expediente", "Mesa de Entradas" }, // mismo asunto: sólo el número las distingue
                { "1002", "3", "Historia clínica", "Historia clínica", null } };

        List<RemitoConDocumentacion> remitos = RemitoConDocumentacion.agrupar(new int[] { 1001, 1002 }, filas);

        assertEquals(List.of(
                new RemitoConDocumentacion(1001, List.of(new DatosDocumentacion(1, "Expediente 1", "Expediente", null),
                        new DatosDocumentacion(2, "Expediente 1", "Expediente", "Mesa de Entradas"))),
                new RemitoConDocumentacion(1002,
                        List.of(new DatosDocumentacion(3, "Historia clínica", "Historia clínica", null)))),
                remitos);
    }
}
