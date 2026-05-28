package pe.com.gamarra360.backend.admin.service;

import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;

public class UsuarioNoEncontradoException extends RecursoNoEncontradoException {
    public UsuarioNoEncontradoException(Integer id) {
        super("Usuario no encontrado con id " + id);
    }
}
