package pe.com.gamarra360.backend.admin.service;

import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;

public class ComercianteNoEncontradoException extends RecursoNoEncontradoException {
    public ComercianteNoEncontradoException(Integer comercianteId) {
        super("Comerciante no encontrado con id " + comercianteId);
    }
}
