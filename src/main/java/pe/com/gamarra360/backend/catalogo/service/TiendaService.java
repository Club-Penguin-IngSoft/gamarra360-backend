package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.service.CrudService;
import pe.com.gamarra360.backend.catalogo.dto.PerfilTiendaPublicaDto;
import pe.com.gamarra360.backend.catalogo.dto.TiendaInfoResponse;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;

public interface TiendaService extends CrudService<Tienda, Integer> {
    /**
     * Obtiene el perfil público de una tienda (solo si está verificada)
     * junto con sus productos activos.
     * Lanza RecursoNoEncontradoException si la tienda no existe o no está verificada.
     */
    PerfilTiendaPublicaDto obtenerPerfilPublico(Integer idTienda);

    TiendaInfoResponse obtenerInfoComerciante(Integer comercianteId);
}