package ppai.bolsines.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ppai.bolsines.modelo.usuario.Sesion;

public interface SesionRepositorio extends JpaRepository<Sesion, Integer> {

    /** La sesión activa es la que no tiene fechaHoraFin. */
    Optional<Sesion> findFirstByFechaHoraFinIsNull();
}
