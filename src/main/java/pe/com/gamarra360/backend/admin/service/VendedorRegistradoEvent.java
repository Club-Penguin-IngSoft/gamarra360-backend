package pe.com.gamarra360.backend.admin.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.com.gamarra360.backend.usuario.entity.Usuario;

@Getter
@AllArgsConstructor
public class VendedorRegistradoEvent {
    private final Usuario usuario;
}