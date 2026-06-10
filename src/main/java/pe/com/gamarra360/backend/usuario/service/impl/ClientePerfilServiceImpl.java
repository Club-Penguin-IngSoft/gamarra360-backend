package pe.com.gamarra360.backend.usuario.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.gamarra360.backend.catalogo.entity.ImagenProducto;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.enums.EstadoPedido;
import pe.com.gamarra360.backend.enums.EstadoSolicitud;
import pe.com.gamarra360.backend.enums.TipoEntrega;
import pe.com.gamarra360.backend.exception.ConflictoNegocioException;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;
import pe.com.gamarra360.backend.pedido.entity.DetallePedido;
import pe.com.gamarra360.backend.pedido.entity.Pedido;
import pe.com.gamarra360.backend.pedido.repository.PedidoRepository;
import pe.com.gamarra360.backend.security.UsuarioPrincipal;
import pe.com.gamarra360.backend.solicitud.entity.*;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionRepository;
import pe.com.gamarra360.backend.solicitud.repository.PersonalizacionRepository;
import pe.com.gamarra360.backend.solicitud.repository.ProductoCotizadoManualRepository;
import pe.com.gamarra360.backend.usuario.dto.perfil.*;
import pe.com.gamarra360.backend.usuario.entity.Cliente;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ClienteRepository;
import pe.com.gamarra360.backend.usuario.repository.NotificacionRepository;
import pe.com.gamarra360.backend.usuario.service.ClientePerfilService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class ClientePerfilServiceImpl implements ClientePerfilService {

    private static final Locale LOCALE_ES_PE = new Locale("es", "PE");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("d MMMM, uuuu", LOCALE_ES_PE);

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final PersonalizacionRepository personalizacionRepository;
    private final CotizacionRepository cotizacionRepository;
    private final ProductoCotizadoManualRepository productoCotizadoManualRepository;
    private final NotificacionRepository notificacionRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientePerfilServiceImpl(ClienteRepository clienteRepository,
                                    PedidoRepository pedidoRepository,
                                    PersonalizacionRepository personalizacionRepository,
                                    CotizacionRepository cotizacionRepository,
                                    ProductoCotizadoManualRepository productoCotizadoManualRepository,
                                    NotificacionRepository notificacionRepository,
                                    PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.personalizacionRepository = personalizacionRepository;
        this.cotizacionRepository = cotizacionRepository;
        this.productoCotizadoManualRepository = productoCotizadoManualRepository;
        this.notificacionRepository = notificacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResumenCuentaResponse obtenerResumenCuenta(UsuarioPrincipal principal) {
        Cliente cliente = obtenerCliente(principal);
        return new ClienteResumenCuentaResponse(
                construirPerfil(cliente),
                listarPedidosRecientes(principal, 2),
                notificacionRepository.countByUsuarioIdAndFueleidaFalse(cliente.getUsuarioId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClientePerfilResponse obtenerPerfil(UsuarioPrincipal principal) {
        return construirPerfil(obtenerCliente(principal));
    }

    @Override
    @Transactional
    public ClientePerfilResponse actualizarPerfil(UsuarioPrincipal principal, ClientePerfilActualizarRequest request) {
        Cliente cliente = obtenerCliente(principal);
        if (request == null) {
            throw new DatosInvalidosException("Debes enviar la información del perfil.");
        }

        actualizarSiTieneTexto(request.nombres(), cliente::setNombres);
        actualizarSiTieneTexto(request.primerApellido(), cliente::setPrimerApellido);
        actualizarSiTieneTexto(request.segundoApellido(), cliente::setSegundoApellido);
        actualizarSiTieneTexto(request.telefono(), cliente::setTelefono);
        actualizarSiTieneTexto(request.dni(), cliente::setDni);
        actualizarSiTieneTexto(request.tipoDocumento(), cliente::setTipoDocumento);
        actualizarSiTieneTexto(request.direccionEntrega(), cliente::setDireccionEntrega);

        if (tieneTexto(request.nombre())) {
            cliente.setNombre(request.nombre().trim());
            if (!tieneTexto(cliente.getNombres())) {
                cliente.setNombres(request.nombre().trim());
            }
        }

        if (tieneTexto(request.apellido())) {
            cliente.setApellido(request.apellido().trim());
            if (!tieneTexto(cliente.getPrimerApellido())) {
                cliente.setPrimerApellido(request.apellido().trim());
            }
        }

        sincronizarCamposCliente(cliente);
        return construirPerfil(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClientePerfilResponse actualizarDireccion(UsuarioPrincipal principal, ClienteDireccionRequest request) {
        Cliente cliente = obtenerCliente(principal);
        if (request == null || !tieneTexto(request.direccionEntrega())) {
            throw new DatosInvalidosException("La dirección de entrega es obligatoria.");
        }
        cliente.setDireccionEntrega(request.direccionEntrega().trim());
        return construirPerfil(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClientePreferenciasNotificacionDto actualizarPreferencias(UsuarioPrincipal principal,
                                                                     ClientePreferenciasNotificacionRequest request) {
        Cliente cliente = obtenerCliente(principal);
        if (request == null) {
            throw new DatosInvalidosException("Debes enviar las preferencias de notificación.");
        }
        if (request.alertasCorreo() != null) {
            cliente.setAlertasCorreo(request.alertasCorreo());
        }
        if (request.notificacionesPush() != null) {
            cliente.setNotificacionesPush(request.notificacionesPush());
        }
        Cliente guardado = clienteRepository.save(cliente);
        return construirPreferencias(guardado);
    }

    @Override
    @Transactional
    public void cambiarPassword(UsuarioPrincipal principal, ClienteCambiarPasswordRequest request) {
        Cliente cliente = obtenerCliente(principal);
        if (request == null || !tieneTexto(request.nuevaContrasenha())) {
            throw new DatosInvalidosException("La nueva contraseña es obligatoria.");
        }
        if (request.nuevaContrasenha().trim().length() < 6) {
            throw new DatosInvalidosException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        String contrasenhaActualCodificada = cliente.getContrasenha();
        if (tieneTexto(contrasenhaActualCodificada)) {
            if (!tieneTexto(request.contrasenhaActual()) ||
                    !passwordEncoder.matches(request.contrasenhaActual(), contrasenhaActualCodificada)) {
                throw new DatosInvalidosException("La contraseña actual no es correcta.");
            }
        }

        cliente.setContrasenha(passwordEncoder.encode(request.nuevaContrasenha().trim()));
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void desactivarCuenta(UsuarioPrincipal principal) {
        Cliente cliente = obtenerCliente(principal);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientePedidoResumenDto> listarPedidos(UsuarioPrincipal principal) {
        Cliente cliente = obtenerCliente(principal);
        return pedidoRepository.findDistinctByClienteIdOrderByFechaDesc(cliente.getUsuarioId())
                .stream()
                .sorted(this::compararPedidosPorFechaDesc)
                .map(this::mapPedidoResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientePedidoResumenDto> listarPedidosRecientes(UsuarioPrincipal principal, int limite) {
        int limiteSeguro = limite <= 0 ? 2 : Math.min(limite, 10);
        return listarPedidos(principal)
                .stream()
                .limit(limiteSeguro)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientePedidoDetalleDto obtenerPedido(UsuarioPrincipal principal, Long pedidoId) {
        Cliente cliente = obtenerCliente(principal);
        Pedido pedido = pedidoRepository.findByIdAndClienteId(pedidoId, cliente.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", pedidoId));
        return mapPedidoDetalle(pedido);
    }

    @Override
    @Transactional
    public ClientePedidoDetalleDto cancelarPedido(UsuarioPrincipal principal, Long pedidoId) {
        Cliente cliente = obtenerCliente(principal);
        Pedido pedido = pedidoRepository.findByIdAndClienteId(pedidoId, cliente.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", pedidoId));

        if (!puedeCancelar(pedido)) {
            throw new ConflictoNegocioException("Este pedido ya no se puede cancelar porque se encuentra en estado "
                    + estadoPedidoTexto(pedido.getEstado()) + ".");
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        return mapPedidoDetalle(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteSolicitudResumenDto> listarPersonalizaciones(UsuarioPrincipal principal) {
        Cliente cliente = obtenerCliente(principal);
        return personalizacionRepository.findDistinctByClienteIdOrderByFechaCreacionDesc(cliente.getUsuarioId())
                .stream()
                .map(this::mapPersonalizacion)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSolicitudResumenDto obtenerPersonalizacion(UsuarioPrincipal principal, Long personalizacionId) {
        Cliente cliente = obtenerCliente(principal);
        Personalizacion personalizacion = personalizacionRepository.findByIdAndClienteId(personalizacionId, cliente.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Personalización", personalizacionId));
        return mapPersonalizacion(personalizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteSolicitudResumenDto> listarCotizaciones(UsuarioPrincipal principal) {
        Cliente cliente = obtenerCliente(principal);
        return cotizacionRepository.findDistinctByClienteIdOrderByFechaCreacionDesc(cliente.getUsuarioId())
                .stream()
                .map(this::mapCotizacion)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSolicitudResumenDto obtenerCotizacion(UsuarioPrincipal principal, Long cotizacionId) {
        Cliente cliente = obtenerCliente(principal);
        Cotizacion cotizacion = cotizacionRepository.findByIdAndClienteId(cotizacionId, cliente.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cotización", cotizacionId));
        return mapCotizacion(cotizacion);
    }


    private int compararPedidosPorFechaDesc(Pedido a, Pedido b) {
        LocalDateTime fechaA = a != null ? a.getFecha() : null;
        LocalDateTime fechaB = b != null ? b.getFecha() : null;
        if (fechaA == null && fechaB == null) return 0;
        if (fechaA == null) return 1;
        if (fechaB == null) return -1;
        return fechaB.compareTo(fechaA);
    }

    private Cliente obtenerCliente(UsuarioPrincipal principal) {
        if (principal == null || principal.getUsuarioId() == null) {
            throw new AccessDeniedException("No hay usuario autenticado.");
        }
        return clienteRepository.findById(principal.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente", Long.valueOf(principal.getUsuarioId())));
    }

    private ClientePerfilResponse construirPerfil(Cliente cliente) {
        String direccion = cliente.getDireccionEntrega();
        if (!tieneTexto(direccion)) {
            direccion = pedidoRepository.findFirstByClienteIdAndDireccionEntregaIsNotNullOrderByFechaDesc(cliente.getUsuarioId())
                    .map(Pedido::getDireccionEntrega)
                    .filter(this::tieneTexto)
                    .orElse(null);
        }
        return new ClientePerfilResponse(
                cliente.getUsuarioId(),
                cliente.getNombres(),
                cliente.getPrimerApellido(),
                cliente.getSegundoApellido(),
                construirNombreCompleto(cliente),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDni(),
                cliente.getTipoDocumento(),
                cliente.getRol() != null ? cliente.getRol().name() : "CLIENTE",
                direccion,
                construirPreferencias(cliente)
        );
    }

    private ClientePreferenciasNotificacionDto construirPreferencias(Cliente cliente) {
        return new ClientePreferenciasNotificacionDto(
                cliente.getAlertasCorreo() == null || cliente.getAlertasCorreo(),
                cliente.getNotificacionesPush() == null ? false : cliente.getNotificacionesPush()
        );
    }

    private ClientePedidoResumenDto mapPedidoResumen(Pedido pedido) {
        Tienda tienda = obtenerTiendaPedido(pedido);
        List<String> imagenes = obtenerDetallesPedido(pedido).stream()
                .map(DetallePedido::getVarianteProducto)
                .filter(Objects::nonNull)
                .map(VarianteProducto::getProducto)
                .filter(Objects::nonNull)
                .map(this::imagenPrincipal)
                .filter(this::tieneTexto)
                .distinct()
                .limit(4)
                .toList();

        return new ClientePedidoResumenDto(
                pedido.getId(),
                generarNumero("PED", pedido.getFecha(), pedido.getId()),
                pedido.getFecha(),
                fechaTexto(pedido.getFecha()),
                pedido.getTotal(),
                pedido.getEstado() != null ? pedido.getEstado().name() : null,
                estadoPedidoTexto(pedido.getEstado()),
                pedido.getTipoEntrega() != null ? pedido.getTipoEntrega().name() : null,
                tipoEntregaTexto(pedido.getTipoEntrega()),
                pedido.getDireccionEntrega(),
                mapTienda(tienda, pedido.getVendedor()),
                imagenes,
                puedeCancelar(pedido)
        );
    }

    private ClientePedidoDetalleDto mapPedidoDetalle(Pedido pedido) {
        Tienda tienda = obtenerTiendaPedido(pedido);
        List<ClienteProductoResumenDto> items = obtenerDetallesPedido(pedido).stream()
                .map(this::mapDetallePedido)
                .toList();

        return new ClientePedidoDetalleDto(
                pedido.getId(),
                generarNumero("PED", pedido.getFecha(), pedido.getId()),
                pedido.getFecha(),
                fechaTexto(pedido.getFecha()),
                pedido.getTotal(),
                pedido.getEstado() != null ? pedido.getEstado().name() : null,
                estadoPedidoTexto(pedido.getEstado()),
                pedido.getTipoEntrega() != null ? pedido.getTipoEntrega().name() : null,
                tipoEntregaTexto(pedido.getTipoEntrega()),
                pedido.getDireccionEntrega(),
                mapTienda(tienda, pedido.getVendedor()),
                items,
                puedeCancelar(pedido)
        );
    }

    private ClienteProductoResumenDto mapDetallePedido(DetallePedido detalle) {
        VarianteProducto variante = detalle.getVarianteProducto();
        Producto producto = variante != null ? variante.getProducto() : null;
        return new ClienteProductoResumenDto(
                producto != null ? producto.getIdProducto() : null,
                variante != null ? variante.getIdVariante() : detalle.getIdVarianteProducto(),
                producto != null ? producto.getNombre() : "Producto",
                producto != null ? imagenPrincipal(producto) : null,
                variante != null && variante.getColor() != null ? variante.getColor().getNombre() : null,
                variante != null && variante.getTalla() != null ? variante.getTalla().getTalla() : null,
                detalle.getCantidad(),
                detalle.getPrecio(),
                detalle.calcularSubtotal()
        );
    }

    private ClienteSolicitudResumenDto mapPersonalizacion(Personalizacion personalizacion) {
        VarianteProducto variante = personalizacion.getVarianteProducto();
        Producto producto = variante != null ? variante.getProducto() : null;
        Tienda tienda = producto != null ? producto.getTienda() : tiendaDesdeVendedor(personalizacion.getVendedor());
        String imagen = tieneTexto(personalizacion.getUrlLogo()) ? personalizacion.getUrlLogo() : (producto != null ? imagenPrincipal(producto) : null);

        ClienteProductoResumenDto item = new ClienteProductoResumenDto(
                producto != null ? producto.getIdProducto() : null,
                variante != null ? variante.getIdVariante() : personalizacion.getDetalleProductoId(),
                producto != null ? producto.getNombre() : "Producto personalizado",
                producto != null ? imagenPrincipal(producto) : personalizacion.getUrlLogo(),
                variante != null && variante.getColor() != null ? variante.getColor().getNombre() : null,
                variante != null && variante.getTalla() != null ? variante.getTalla().getTalla() : null,
                null,
                precioVariante(variante),
                null
        );

        return new ClienteSolicitudResumenDto(
                personalizacion.getId(),
                generarNumero("PER", personalizacion.getFechaCreacion(), personalizacion.getId()),
                personalizacion.getFechaCreacion(),
                fechaTexto(personalizacion.getFechaCreacion()),
                personalizacion.getEstado() != null ? personalizacion.getEstado().name() : null,
                estadoSolicitudTexto(personalizacion.getEstado()),
                personalizacion.getTipoPersonalizacion() != null ? personalizacion.getTipoPersonalizacion().name() : "PERSONALIZACION",
                personalizacion.getDescripcion(),
                imagen,
                null,
                mapTienda(tienda, personalizacion.getVendedor()),
                List.of(item)
        );
    }

    private ClienteSolicitudResumenDto mapCotizacion(Cotizacion cotizacion) {
        List<ClienteProductoResumenDto> items = cotizacion.getListaDetallesCotizacion() == null
                ? List.of()
                : cotizacion.getListaDetallesCotizacion().stream()
                .map(this::mapDetalleCotizacion)
                .toList();

        String imagen = items.stream()
                .map(ClienteProductoResumenDto::imagenUrl)
                .filter(this::tieneTexto)
                .findFirst()
                .orElse(null);

        Tienda tienda = cotizacion.getTienda() != null ? cotizacion.getTienda() : tiendaDesdeVendedor(cotizacion.getVendedor());

        return new ClienteSolicitudResumenDto(
                cotizacion.getId(),
                generarNumero("COT", cotizacion.getFechaCreacion(), cotizacion.getId()),
                cotizacion.getFechaCreacion(),
                fechaTexto(cotizacion.getFechaCreacion()),
                cotizacion.getEstado() != null ? cotizacion.getEstado().name() : null,
                estadoSolicitudTexto(cotizacion.getEstado()),
                "COTIZACION",
                "Cotización solicitada",
                imagen,
                cotizacion.getRespuestaSolicitud() != null ? cotizacion.getRespuestaSolicitud().getPrecioPropuesto() : null,
                mapTienda(tienda, cotizacion.getVendedor()),
                items
        );
    }

    private ClienteProductoResumenDto mapDetalleCotizacion(DetalleCotizacion detalle) {
        DetalleCotizacion detalleReal = (DetalleCotizacion) Hibernate.unproxy(detalle);
        if (detalleReal instanceof CotizacionCatalogo catalogo) {
            VarianteProducto variante = catalogo.getVarianteProducto();
            Producto producto = variante != null ? variante.getProducto() : null;
            return new ClienteProductoResumenDto(
                    producto != null ? producto.getIdProducto() : null,
                    variante != null ? variante.getIdVariante() : catalogo.getIdDetalleProducto(),
                    producto != null ? producto.getNombre() : "Producto de catálogo",
                    producto != null ? imagenPrincipal(producto) : null,
                    variante != null && variante.getColor() != null ? variante.getColor().getNombre() : null,
                    variante != null && variante.getTalla() != null ? variante.getTalla().getTalla() : null,
                    catalogo.getCantidad(),
                    catalogo.getPrecioBase(),
                    subtotal(catalogo.getCantidad(), catalogo.getPrecioBase())
            );
        }

        Optional<ProductoCotizadoManual> manual = productoCotizadoManualRepository.findByDetalleCotizacionId(detalleReal.getDetalleCotizacionId());
        return new ClienteProductoResumenDto(
                null,
                null,
                manual.map(ProductoCotizadoManual::getNombre).orElse("Producto cotizado"),
                manual.map(ProductoCotizadoManual::getImagen).orElse(null),
                null,
                null,
                detalleReal.getCantidad(),
                detalleReal.getPrecioBase(),
                subtotal(detalleReal.getCantidad(), detalleReal.getPrecioBase())
        );
    }

    private List<DetallePedido> obtenerDetallesPedido(Pedido pedido) {
        return pedido.getListaDetalles() == null ? List.of() : pedido.getListaDetalles();
    }

    private Tienda obtenerTiendaPedido(Pedido pedido) {
        Tienda tienda = tiendaDesdeVendedor(pedido.getVendedor());
        if (tienda != null) {
            return tienda;
        }
        return obtenerDetallesPedido(pedido).stream()
                .map(DetallePedido::getVarianteProducto)
                .filter(Objects::nonNull)
                .map(VarianteProducto::getProducto)
                .filter(Objects::nonNull)
                .map(Producto::getTienda)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Tienda tiendaDesdeVendedor(Comerciante vendedor) {
        return vendedor != null ? vendedor.getTienda() : null;
    }

    private ClienteTiendaResumenDto mapTienda(Tienda tienda, Comerciante vendedor) {
        if (tienda != null) {
            return new ClienteTiendaResumenDto(tienda.getIdTienda(), tienda.getNombreComercial(), tienda.getFoto());
        }
        if (vendedor != null) {
            return new ClienteTiendaResumenDto(null, vendedor.getNombreTienda(), null);
        }
        return null;
    }

    private String imagenPrincipal(Producto producto) {
        if (producto == null || producto.getImagenes() == null || producto.getImagenes().isEmpty()) {
            return null;
        }
        return producto.getImagenes().stream()
                .filter(img -> Boolean.TRUE.equals(img.getEsPrincipal()))
                .map(ImagenProducto::getUrl)
                .filter(this::tieneTexto)
                .findFirst()
                .or(() -> producto.getImagenes().stream()
                        .map(ImagenProducto::getUrl)
                        .filter(this::tieneTexto)
                        .findFirst())
                .orElse(null);
    }

    private boolean puedeCancelar(Pedido pedido) {
        return pedido != null && (pedido.getEstado() == EstadoPedido.PENDIENTE_CONFIRMACION
                || pedido.getEstado() == EstadoPedido.CONFIRMADO);
    }

    private String estadoPedidoTexto(EstadoPedido estado) {
        if (estado == null) return "Pendiente";
        return switch (estado) {
            case PENDIENTE_CONFIRMACION -> "Pendiente de confirmación";
            case CONFIRMADO -> "Confirmado";
            case EN_PREPARACION -> "En preparación";
            case LISTO_PARA_ENTREGA -> "Listo para entrega";
            case RECIBIDO -> "Recibido";
            case CANCELADO -> "Cancelado";
        };
    }

    private String estadoSolicitudTexto(EstadoSolicitud estado) {
        if (estado == null) return "Pendiente";
        return switch (estado) {
            case PENDIENTE -> "Pendiente";
            case RESPONDIDA -> "Respondida";
            case ACEPTADA -> "Aceptada";
            case RECHAZADA -> "Rechazada";
        };
    }

    private String tipoEntregaTexto(TipoEntrega tipoEntrega) {
        if (tipoEntrega == null) return null;
        return switch (tipoEntrega) {
            case DELIVERY -> "Envío a domicilio";
            case RECOJO_TIENDA -> "Recojo en tienda";
        };
    }

    private String generarNumero(String prefijo, LocalDateTime fecha, Long id) {
        String fechaParte = fecha != null ? fecha.format(DateTimeFormatter.BASIC_ISO_DATE) : "00000000";
        return prefijo + "-" + fechaParte + "-" + String.format("%06d", id == null ? 0 : id);
    }

    private String fechaTexto(LocalDateTime fecha) {
        if (fecha == null) return null;
        String texto = fecha.format(FECHA_CORTA);
        return capitalizarMes(texto);
    }

    private String capitalizarMes(String texto) {
        if (!tieneTexto(texto)) return texto;
        String[] partes = texto.split(" ");
        for (int i = 0; i < partes.length; i++) {
            if (partes[i].length() > 1 && Character.isLetter(partes[i].charAt(0))) {
                partes[i] = partes[i].substring(0, 1).toUpperCase(LOCALE_ES_PE) + partes[i].substring(1);
            }
        }
        return String.join(" ", partes);
    }

    private String construirNombreCompleto(Cliente cliente) {
        List<String> partes = new ArrayList<>();
        agregarSiTieneTexto(partes, cliente.getNombres());
        agregarSiTieneTexto(partes, cliente.getPrimerApellido());
        agregarSiTieneTexto(partes, cliente.getSegundoApellido());
        if (partes.isEmpty()) {
            agregarSiTieneTexto(partes, cliente.getNombre());
            agregarSiTieneTexto(partes, cliente.getApellido());
        }
        return String.join(" ", partes);
    }

    private void sincronizarCamposCliente(Cliente cliente) {
        if (!tieneTexto(cliente.getNombre()) && tieneTexto(cliente.getNombres())) {
            cliente.setNombre(cliente.getNombres().trim());
        }
        if (!tieneTexto(cliente.getApellido()) && tieneTexto(cliente.getPrimerApellido())) {
            cliente.setApellido(cliente.getPrimerApellido().trim());
        }
    }

    private Double precioVariante(VarianteProducto variante) {
        if (variante == null) return null;
        if (variante.getPrecioAjustado() != null) return variante.getPrecioAjustado();
        Producto producto = variante.getProducto();
        return producto != null ? producto.getPrecioBase() : null;
    }

    private Double subtotal(Integer cantidad, Double precio) {
        return cantidad == null || precio == null ? null : cantidad * precio;
    }

    private void agregarSiTieneTexto(List<String> partes, String valor) {
        if (tieneTexto(valor)) {
            partes.add(valor.trim());
        }
    }

    private void actualizarSiTieneTexto(String valor, java.util.function.Consumer<String> setter) {
        if (tieneTexto(valor)) {
            setter.accept(valor.trim());
        }
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
