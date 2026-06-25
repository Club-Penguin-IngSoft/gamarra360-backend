package pe.com.gamarra360.backend.catalogo.service;

import pe.com.gamarra360.backend.catalogo.dto.OfertaRequestDto;
import pe.com.gamarra360.backend.catalogo.dto.OfertaResponseDto;

import java.util.List;

public interface OfertaService {
    List<OfertaResponseDto> listar(Integer comercianteId);
    OfertaResponseDto obtener(Integer idOferta, Integer comercianteId);
    OfertaResponseDto crear(OfertaRequestDto request, Integer comercianteId);
    OfertaResponseDto actualizar(Integer idOferta, OfertaRequestDto request, Integer comercianteId);
    void eliminar(Integer idOferta, Integer comercianteId);
    OfertaResponseDto toggleActiva(Integer idOferta, Integer comercianteId);
}
