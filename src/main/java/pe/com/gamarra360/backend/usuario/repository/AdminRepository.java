package pe.com.gamarra360.backend.usuario.repository;

import pe.com.gamarra360.backend.usuario.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
}
