package pe.com.gamarra360.backend.logistica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.logistica.dto.*;
import pe.com.gamarra360.backend.logistica.entity.DistritoEnvio;
import pe.com.gamarra360.backend.logistica.entity.TarifaEnvioTienda;
import pe.com.gamarra360.backend.logistica.repository.DistritoEnvioRepository;
import pe.com.gamarra360.backend.logistica.repository.TarifaEnvioTiendaRepository;
import java.util.List;

@Service @RequiredArgsConstructor
public class TarifaEnvioService {
    private final TarifaEnvioTiendaRepository repository;
    private final TiendaRepository tiendaRepository;
    private final DistritoEnvioRepository distritoRepository;
    @Transactional(readOnly=true) public List<TarifaEnvioResponse> listarVendedor(Integer vendedorId){ return listarTienda(tienda(vendedorId).getIdTienda()); }
    @Transactional(readOnly=true) public List<TarifaEnvioResponse> listarTienda(Integer tiendaId){ return repository.findByTiendaIdOrderByDistritoId(tiendaId).stream().map(this::map).toList(); }
    @Transactional public TarifaEnvioResponse guardar(Integer vendedorId, TarifaEnvioRequest req){
        Tienda t=tienda(vendedorId); distritoRepository.findById(req.distritoId()).orElseThrow(()->new RecursoNoEncontradoException("Distrito",req.distritoId()));
        TarifaEnvioTienda tarifa=repository.findByTiendaIdAndDistritoId(t.getIdTienda(),req.distritoId()).orElseGet(TarifaEnvioTienda::new);
        tarifa.setTiendaId(t.getIdTienda()); tarifa.setDistritoId(req.distritoId()); tarifa.setCostoEnvio(req.costoEnvio()); tarifa.setActivo(req.activo()==null||req.activo()); return map(repository.save(tarifa));
    }
    @Transactional(readOnly=true) public double resolver(Integer vendedorId,Integer distritoId,double fallback){
        return tiendaRepository.findByIdComerciante(vendedorId).flatMap(t->repository.findByTiendaIdAndDistritoId(t.getIdTienda(),distritoId)).filter(t->Boolean.TRUE.equals(t.getActivo())).map(TarifaEnvioTienda::getCostoEnvio).orElse(fallback);
    }
    private Tienda tienda(Integer v){return tiendaRepository.findByIdComerciante(v).orElseThrow(()->new RecursoNoEncontradoException("Tienda",v));}
    private TarifaEnvioResponse map(TarifaEnvioTienda t){ DistritoEnvio d=distritoRepository.findById(t.getDistritoId()).orElse(null); return new TarifaEnvioResponse(t.getId(),t.getDistritoId(),d!=null?d.getNombre():null,t.getCostoEnvio(),t.getActivo()); }
}
