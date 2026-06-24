package pe.com.gamarra360.backend.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.gamarra360.backend.usuario.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
