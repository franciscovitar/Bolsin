package ppai.bolsines.modelo.documentacion;

import java.util.Map;
import java.util.function.Supplier;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persistencia del patrón State: en la base se guarda el nombre del estado (una columna) y al
 * leer se crea el objeto del estado concreto que corresponde. Así no hace falta una tabla por
 * cada estado y en el código se sigue usando new EnBolsinEnviado(), new RecibidaYAceptada(), etc.
 */
@Converter
public class EstadoDocumentacionConverter implements AttributeConverter<EstadoDocumentacion, String> {

    private static final Map<String, Supplier<EstadoDocumentacion>> ESTADOS = Map.of(
            "Registrada", Registrada::new,
            "EnRemito", EnRemito::new,
            "EnBolsinSaliente", EnBolsinSaliente::new,
            "EnBolsinEnviado", EnBolsinEnviado::new,
            "RecibidaYAceptada", RecibidaYAceptada::new,
            "RecibidaYRechazada", RecibidaYRechazada::new,
            "NoRecibida", NoRecibida::new,
            "ParaRedirigir", ParaRedirigir::new,
            "DeBaja", DeBaja::new);

    @Override
    public String convertToDatabaseColumn(EstadoDocumentacion estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public EstadoDocumentacion convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        Supplier<EstadoDocumentacion> estado = ESTADOS.get(nombre);
        if (estado == null) {
            throw new IllegalArgumentException("Estado de documentación desconocido: " + nombre);
        }
        return estado.get();
    }
}
