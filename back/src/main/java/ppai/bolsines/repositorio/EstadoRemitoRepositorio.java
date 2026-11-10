package ppai.bolsines.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ppai.bolsines.modelo.remito.EstadoRemito;

public interface EstadoRemitoRepositorio extends JpaRepository<EstadoRemito, Integer> {
}
