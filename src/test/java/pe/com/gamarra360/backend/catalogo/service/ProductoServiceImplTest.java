package pe.com.gamarra360.backend.catalogo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.gamarra360.backend.catalogo.dto.ProductoRequest;
import pe.com.gamarra360.backend.catalogo.dto.ProductoResponse;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.catalogo.entity.MaterialFiltro;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;
import pe.com.gamarra360.backend.catalogo.repository.CategoriaRepository;
import pe.com.gamarra360.backend.catalogo.repository.ColorRepository;
import pe.com.gamarra360.backend.catalogo.repository.EspecificacionRepository;
import pe.com.gamarra360.backend.catalogo.repository.ImagenProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.MaterialFiltroRepository;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.TallaRepository;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.repository.TipoProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.impl.ProductoServiceImpl;
import pe.com.gamarra360.backend.pedido.repository.DetallePedidoRepository;
import pe.com.gamarra360.backend.solicitud.repository.CotizacionCatalogoRepository;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import pe.com.gamarra360.backend.catalogo.dto.FiltrosCatalogoDto;
import pe.com.gamarra360.backend.catalogo.dto.PaginaResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock private ProductoRepository          productoRepository;
    @Mock private ComercianteRepository       comercianteRepository;
    @Mock private TiendaRepository            tiendaRepository;
    @Mock private CategoriaRepository         categoriaRepository;
    @Mock private TipoProductoRepository      tipoProductoRepository;
    @Mock private ImagenProductoRepository    imagenProductoRepository;
    @Mock private VarianteProductoRepository  varianteProductoRepository;
    @Mock private DetallePedidoRepository     detallePedidoRepository;
    @Mock private CotizacionCatalogoRepository cotizacionCatalogoRepository;
    @Mock private ColorRepository             colorRepository;
    @Mock private TallaRepository             tallaRepository;
    @Mock private EspecificacionRepository    especificacionRepository;
    @Mock private MaterialFiltroRepository    materialFiltroRepository;

    @InjectMocks
    private ProductoServiceImpl service;

    private Producto  producto;
    private Tienda    tienda;
    private Categoria categoria;
    private TipoProducto tipoProducto;
    private ProductoRequest request;

    @BeforeEach
    void setUp() {
        tienda = new Tienda();
        tienda.setIdTienda(5);
        tienda.setIdComerciante(1);

        producto = new Producto();
        producto.setIdProducto(10);
        producto.setActivo(true);
        producto.setIdTienda(5);
        producto.setTienda(tienda);
        producto.setNombre("Buzo Algodón");
        producto.setPrecioBase(29.0);

        categoria = new Categoria();
        categoria.setIdCategoria(1);

        tipoProducto = new TipoProducto();
        tipoProducto.setIdTipoProducto(2);

        // guardarImagenes() itera la lista sin null-check → necesita al menos un elemento
        pe.com.gamarra360.backend.catalogo.dto.ImagenRequest img =
                new pe.com.gamarra360.backend.catalogo.dto.ImagenRequest();
        img.setUrl("https://test.com/img.jpg");
        img.setEsPrincipal(true);

        request = new ProductoRequest();
        request.setNombre("Buzo Algodón");
        request.setDescripcion("Buzo de algodón premium");
        request.setPrecioBase(29.0);
        request.setEsPersonalizable(false);
        request.setIdCategoria(1);
        request.setIdTipoProducto(2);
        request.setImagenes(java.util.List.of(img));
    }

    /** Stubs mínimos que siempre necesita actualizarProducto() para llegar a buildResponse(). */
    private void stubComunes() {
        when(productoRepository.findById(10)).thenReturn(Optional.of(producto));
        when(tiendaRepository.findByIdComerciante(1)).thenReturn(Optional.of(tienda));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(tipoProductoRepository.findById(2)).thenReturn(Optional.of(tipoProducto));
        when(imagenProductoRepository.findByIdProducto(10)).thenReturn(Collections.emptyList());
        when(imagenProductoRepository.save(any(pe.com.gamarra360.backend.catalogo.entity.ImagenProducto.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(varianteProductoRepository.findByIdProducto(10)).thenReturn(Collections.emptyList());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── GET /api/v1/productos/{id} → idMaterial en la respuesta ──────────────

    @Test
    @DisplayName("debeDevolverIdMaterialNulo_cuandoElProductoNoTieneMaterialAsignado")
    void debeDevolverIdMaterialNulo_cuandoElProductoNoTieneMaterialAsignado() {
        stubComunes();
        request.setIdMaterialFiltro(null); // sin material

        ProductoResponse response = service.actualizarProducto(10, request, 1);

        assertThat(response.getIdMaterial()).isNull();
        assertThat(response.getMaterialPrincipal()).isNull();
        verify(materialFiltroRepository, never()).findById(any());
    }

    // ── GET /api/v1/productos?q= → búsqueda por keyword en listarConFiltros ────

    @Test
    @DisplayName("debeFiltrarPorNombreOdescripcion_cuandoSeEnviaParametroQ")
    void debeFiltrarPorNombreOdescripcion_cuandoSeEnviaParametroQ() {
        Producto p = crearProducto(20, "Buzo Algodón Premium");
        Page<Producto> pagina = new PageImpl<>(List.of(p), PageRequest.of(0, 12), 1);
        when(productoRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pagina);
        stubToResponse(20);

        FiltrosCatalogoDto filtros = new FiltrosCatalogoDto();
        filtros.setQ("algodón");

        PaginaResponse<ProductoResponse> response = service.listarConFiltros(filtros);

        assertThat(response.getTotalElementos()).isEqualTo(1);
        assertThat(response.getContenido()).hasSize(1);
        assertThat(response.getContenido().get(0).getNombre()).isEqualTo("Buzo Algodón Premium");
        verify(productoRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("debeCombinarQConOtrosFiltros_cuandoAmbosEstanPresentes")
    void debeCombinarQConOtrosFiltros_cuandoAmbosEstanPresentes() {
        Producto p1 = crearProducto(21, "Polo Oversize");
        Producto p2 = crearProducto(22, "Polo Básico");
        Page<Producto> pagina = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 12), 2);
        when(productoRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pagina);
        stubToResponse(21, 22);

        FiltrosCatalogoDto filtros = new FiltrosCatalogoDto();
        filtros.setQ("polo");
        filtros.setCategorias(List.of("Camisas"));

        PaginaResponse<ProductoResponse> response = service.listarConFiltros(filtros);

        assertThat(response.getTotalElementos()).isEqualTo(2);
        assertThat(response.getContenido()).hasSize(2);
        assertThat(response.getPaginaActual()).isZero();
        verify(productoRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("debeDevolverListaVacia_cuandoQNoCoincideConNingunProducto")
    void debeDevolverListaVacia_cuandoQNoCoincideConNingunProducto() {
        Page<Producto> pagina = new PageImpl<>(List.of(), PageRequest.of(0, 12), 0);
        when(productoRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pagina);

        FiltrosCatalogoDto filtros = new FiltrosCatalogoDto();
        filtros.setQ("xyzxyz_sin_resultados");

        PaginaResponse<ProductoResponse> response = service.listarConFiltros(filtros);

        assertThat(response.getTotalElementos()).isZero();
        assertThat(response.getTotalPaginas()).isZero();
        assertThat(response.getContenido()).isEmpty();
    }

    // ── Helpers para tests de listarConFiltros ───────────────────────────────

    /** Crea un Producto mínimo con tienda asignada para que toResponse() funcione. */
    private Producto crearProducto(int id, String nombre) {
        Tienda t = new Tienda();
        t.setIdTienda(1);
        t.setIdComerciante(1);

        Producto p = new Producto();
        p.setIdProducto(id);
        p.setNombre(nombre);
        p.setPrecioBase(50.0);
        p.setActivo(true);
        p.setTienda(t);
        return p;
    }

    /** Stubs los repositorios de lectura que toResponse() necesita para cada producto. */
    private void stubToResponse(int... idProductos) {
        for (int id : idProductos) {
            when(imagenProductoRepository.findByIdProducto(id)).thenReturn(Collections.emptyList());
            when(especificacionRepository.findByIdProducto(id)).thenReturn(Collections.emptyList());
            when(varianteProductoRepository.findByIdProducto(id)).thenReturn(Collections.emptyList());
        }
    }

    // ── PUT /api/v1/productos/{id} → actualiza la relación Material ───────────

    @Test
    @DisplayName("debeActualizarMaterialDelProducto_cuandoSeEnviaIdMaterialEnElUpdate")
    void debeActualizarMaterialDelProducto_cuandoSeEnviaIdMaterialEnElUpdate() {
        stubComunes();

        MaterialFiltro material = new MaterialFiltro();
        material.setIdMaterial(5);
        material.setNombre("Algodón");
        when(materialFiltroRepository.findById(5)).thenReturn(Optional.of(material));

        request.setIdMaterialFiltro(5);

        ProductoResponse response = service.actualizarProducto(10, request, 1);

        assertThat(response.getIdMaterial()).isEqualTo(5);
        assertThat(response.getMaterialPrincipal()).isEqualTo("Algodón");
        verify(materialFiltroRepository).findById(5);
    }
}
