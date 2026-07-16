package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;

import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.security.JwtService;
import pe.com.gamarra360.backend.service.AbstractCrudService;
import pe.com.gamarra360.backend.usuario.dto.PerfilComercianteDto;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;
import pe.com.gamarra360.backend.usuario.service.ComercianteService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.usuario.service.NotificacionService;

import java.util.List;

@Service
@Slf4j
public class ComercianteServiceImpl extends AbstractCrudService<Comerciante, Integer> implements ComercianteService {

    private final ComercianteRepository comercianteRepository;
    private final TiendaRepository tiendaRepository;
    private final NotificacionService notificacionService;
    private final JwtService jwtService;

    public ComercianteServiceImpl(ComercianteRepository repository, TiendaRepository tiendaRepository, NotificacionService notificacionService, JwtService jwtService) {
        super(repository, "Comerciante");
        this.comercianteRepository = repository;
        this.tiendaRepository = tiendaRepository;
        this.notificacionService = notificacionService;
        this.jwtService = jwtService;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    protected void asignarId(Comerciante entidad, Integer id) {
        entidad.setUsuarioId(id);
    }

    @Override
    public List<Comerciante> listarPendientes() {
        log.info("Listando comerciantes pendientes de aprobacion");
        return comercianteRepository.findByVerificadoFalse();
    }
    private Integer getActorId() {
        return jwtService.getUserIdFromContext();
    }
    @Override
    @Transactional
    public Comerciante aprobar(Integer id) {
        log.info("Aprobando comerciante con id {}", id);
        Comerciante comerciante = obtener(id);
        comerciante.setVerificado(true);
        comerciante.setActivo(true);
        comerciante.setAprobado(true);
        comercianteRepository.save(comerciante);
        Integer actorId = getActorId();
        notificacionService.crearNotificacion(
                comerciante.getUsuarioId(),              // receptor
                actorId,                                       // actor (ADMIN - puedes cambiarlo dinámico)
                "Tu cuenta fue APROBADA",
                "COMERCIANTE",
                comerciante.getUsuarioId().longValue(),  // referencia
                "USUARIO",
                "APROBADO",
                "/perfil"
        );
        // La Tienda fue creada en el registro; aquí solo la actualizamos si ya existe,
        // o la creamos como fallback para comerciantes registrados antes de este cambio.
        tiendaRepository.findByIdComerciante(comerciante.getUsuarioId()).ifPresentOrElse(
                tienda -> {
                    // ya existe: solo marcar como no-verificada (el admin la verificará después)
                    if (tienda.getNombreComercial() == null) {
                        tienda.setNombreComercial(comerciante.getNombreTienda() != null
                                ? comerciante.getNombreTienda()
                                : comerciante.getRazonSocial());
                        tiendaRepository.save(tienda);
                    }
                },
                () -> {
                    Tienda tienda = new Tienda();
                    tienda.setIdComerciante(comerciante.getUsuarioId());
                    tienda.setNombreComercial(comerciante.getNombreTienda() != null
                            ? comerciante.getNombreTienda()
                            : comerciante.getRazonSocial());
                    tienda.setVerificada(false);
                    tiendaRepository.save(tienda);
                }
        );

        return obtener(id);
    }

    @Override
    @Transactional
    public void rechazar(Integer id) {
        log.info("Rechazando/eliminando comerciante con id {}", id);
        Comerciante comerciante = obtener(id);
        Integer actorId = getActorId();
        notificacionService.crearNotificacion(
                comerciante.getUsuarioId(),   // receptor
                actorId,                            // actor (ADMIN)
                "Tu solicitud fue RECHAZADA",
                "COMERCIANTE",
                comerciante.getUsuarioId().longValue(),
                "USUARIO",
                "RECHAZADO",
                "/perfil"
        );

        eliminar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilComercianteDto obtenerPerfil(Integer comercianteId) {
        Comerciante c = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado con id " + comercianteId));
        Tienda tienda = tiendaRepository.findByIdComerciante(comercianteId).orElse(null);

        PerfilComercianteDto dto = new PerfilComercianteDto();
        dto.setEmail(c.getEmail());
        dto.setNombres(c.getNombres());
        dto.setPrimerApellido(c.getPrimerApellido());
        dto.setSegundoApellido(c.getSegundoApellido());
        dto.setTipoDocumento(c.getTipoDocumento());
        dto.setDni(c.getDni());
        dto.setTelefono(c.getTelefono());
        dto.setRazonSocial(c.getRazonSocial());
        dto.setRuc(c.getRuc());
        dto.setLogoUrl(c.getLogoUrl());
        dto.setVerificada(Boolean.TRUE.equals(c.getVerificado()));
        if (tienda != null) {
            dto.setNombreTienda(tienda.getNombreComercial());
            dto.setInformacion(tienda.getInformacion());
            dto.setGaleria(tienda.getGaleria());
            dto.setPiso(tienda.getPiso());
            dto.setStand(tienda.getStand());
            dto.setFoto(tienda.getFoto());
            dto.setOfreceEnvioDomicilio(tienda.getOfreceEnvioDomicilio());
        }
        return dto;
    }

    @Override
    @Transactional
    public PerfilComercianteDto actualizarPerfil(Integer comercianteId, PerfilComercianteDto dto) {
        Comerciante c = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comerciante no encontrado con id " + comercianteId));

        if (dto.getNombres() != null)        c.setNombres(dto.getNombres());
        if (dto.getPrimerApellido() != null) c.setPrimerApellido(dto.getPrimerApellido());
        if (dto.getSegundoApellido() != null) c.setSegundoApellido(dto.getSegundoApellido());
        if (dto.getTipoDocumento() != null)  c.setTipoDocumento(dto.getTipoDocumento());
        if (dto.getDni() != null)           c.setDni(dto.getDni());
        if (dto.getTelefono() != null)      c.setTelefono(dto.getTelefono());
        if (dto.getRazonSocial() != null)   c.setRazonSocial(dto.getRazonSocial());
        if (dto.getLogoUrl() != null)       c.setLogoUrl(dto.getLogoUrl());
        comercianteRepository.save(c);

        tiendaRepository.findByIdComerciante(comercianteId).ifPresent(tienda -> {
            if (dto.getNombreTienda() != null)        tienda.setNombreComercial(dto.getNombreTienda());
            if (dto.getInformacion() != null)         tienda.setInformacion(dto.getInformacion());
            if (dto.getGaleria() != null)             tienda.setGaleria(dto.getGaleria());
            if (dto.getPiso() != null)                tienda.setPiso(dto.getPiso());
            if (dto.getStand() != null)               tienda.setStand(dto.getStand());
            if (dto.getFoto() != null)                tienda.setFoto(dto.getFoto());
            if (dto.getOfreceEnvioDomicilio() != null) tienda.setOfreceEnvioDomicilio(dto.getOfreceEnvioDomicilio());
            tiendaRepository.save(tienda);
        });

        return obtenerPerfil(comercianteId);
    }
}
