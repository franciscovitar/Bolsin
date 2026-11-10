package ppai.bolsines.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ppai.bolsines.modelo.bolsin.Bolsin;

public interface BolsinRepositorio extends JpaRepository<Bolsin, Integer> {
}
