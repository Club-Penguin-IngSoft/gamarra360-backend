package pe.com.gamarra360.backend.admin.service;

import pe.com.gamarra360.backend.exception.DatosInvalidosException;

public class EstadoInvalidoException extends DatosInvalidosException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
