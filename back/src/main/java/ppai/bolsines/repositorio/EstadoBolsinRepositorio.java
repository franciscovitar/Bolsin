package ppai.bolsines.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ppai.bolsines.modelo.bolsin.EstadoBolsin;

public interface EstadoBolsinRepositorio extends JpaRepository<EstadoBolsin, Integer> {
}
