package ppai.bolsines.dto;

import jakarta.validation.constraints.NotBlank;

public record SeleccionOpcionRequest(@NotBlank String opcion) {
}
