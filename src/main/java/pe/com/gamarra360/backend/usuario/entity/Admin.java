package pe.com.gamarra360.backend.usuario.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
public class Admin extends Usuario {
    public Admin() {
    }
}
