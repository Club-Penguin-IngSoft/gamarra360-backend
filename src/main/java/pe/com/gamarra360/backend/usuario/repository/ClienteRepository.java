package pe.com.gamarra360.backend.usuario.repository;

import pe.com.gamarra360.backend.usuario.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
