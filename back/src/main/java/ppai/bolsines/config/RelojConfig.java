package ppai.bolsines.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hora del sistema como bean, igual que en el TPI: el gestor la usa en getFechaHoraActual()
 * y en los tests se reemplaza por un reloj fijo.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
