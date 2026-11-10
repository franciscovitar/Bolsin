package ppai.bolsines.dto;

/**
 * Respuesta de tomarSeleccionOpcionRecepcion: el mensaje de la pantalla y si el siguiente paso es
 * marcar documentación (A3: no recibida, A4: no corresponde al destino, A5: redirigir a otra área)
 * o confirmar el registro.
 */
public record SeleccionOpcionResponse(String mensaje, boolean seleccionarDocumentacion) {
}
