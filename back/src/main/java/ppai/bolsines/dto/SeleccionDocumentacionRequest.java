package ppai.bolsines.dto;

import jakarta.validation.constraints.NotEmpty;

/** A3 y A4: números de la documentación que el EB marcó (no recibida o que no corresponde al destino). */
public record SeleccionDocumentacionRequest(@NotEmpty int[] nrosDocumentacion) {
}
