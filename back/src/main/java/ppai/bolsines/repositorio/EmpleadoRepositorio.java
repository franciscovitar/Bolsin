package ppai.bolsines.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ppai.bolsines.modelo.usuario.Empleado;

public interface EmpleadoRepositorio extends JpaRepository<Empleado, Integer> {
}
