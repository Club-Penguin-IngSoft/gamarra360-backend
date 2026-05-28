package pe.com.gamarra360.backend.admin.service;

import pe.com.gamarra360.backend.exception.DatosInvalidosException;

public class TransicionEstadoInvalidaException extends DatosInvalidosException {
    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
