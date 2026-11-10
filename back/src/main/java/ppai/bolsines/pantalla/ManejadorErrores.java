package ppai.bolsines.pantalla;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ppai.bolsines.dto.MensajeResponse;

/**
 * Traduce las excepciones a respuestas con un mensaje para el actor:
 * 400 datos inválidos (precinto inexistente, opción desconocida), 409 operación que no
 * corresponde en el estado actual (por ejemplo, una transición que el estado rechaza).
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentNotValidException.class })
    public ResponseEntity<MensajeResponse> datosInvalidos(Exception e) {
        return ResponseEntity.badRequest().body(new MensajeResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MensajeResponse> estadoInvalido(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MensajeResponse(e.getMessage()));
    }
}
