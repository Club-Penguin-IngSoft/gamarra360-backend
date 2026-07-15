package pe.com.gamarra360.backend.catalogo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pe.com.gamarra360.backend.IntegrationTestBase;
import pe.com.gamarra360.backend.catalogo.entity.Categoria;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.entity.TipoProducto;
import pe.com.gamarra360.backend.catalogo.repository.CategoriaRepository;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.repository.TipoProductoRepository;
import pe.com.gamarra360.backend.enums.RolEnum;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BusquedaProductoIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private TiendaRepository tiendaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TipoProductoRepository tipoProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void setUp() {
        sembrarDatosPrueba();
    }

    private void sembrarDatosPrueba() {
        // 1. Un Comerciante verificado=true y activo=true
        Comerciante comerciante = new Comerciante();
        comerciante.setNombreComerciante("Juan");
        comerciante.setApellidoComerciante("Perez");
        comerciante.setRazonSocial("Juan Perez S.A.C.");
        comerciante.setRuc("20123456789");
        comerciante.setVerificado(true);
        comerciante.setAprobado(true);
        comerciante.setEmail("comerciante.busqueda@gamarra360.com");
        comerciante.setActivo(true);
        comerciante.setRol(RolEnum.VENDEDOR);
        comerciante.setTipoDocumento("RUC");
        comerciante = comercianteRepository.save(comerciante);

        // 2. Una Tienda verificada=true asociada a ese comerciante
        Tienda tienda = new Tienda();
        tienda.setIdComerciante(comerciante.getUsuarioId());
        tienda.setNombreComercial("Tienda de Juan");
        tienda.setInformacion("La mejor tienda");
        tienda.setVerificada(true);
        tienda = tiendaRepository.save(tienda);

        // 3. Categoría y tipo de producto mínimos necesarios
        Categoria categoria = new Categoria();
        categoria.setNombreCategoria("Moda");
        categoria.setDescripcion("Ropa en general");
        categoria = categoriaRepository.save(categoria);

        TipoProducto tipoProducto = new TipoProducto();
        tipoProducto.setNombre("Prendas");
        tipoProducto.setCategoria(categoria);
        tipoProducto = tipoProductoRepository.save(tipoProducto);

        // 4. Siembra de productos
        // Producto 1: "Polo Jersey Premium", descripcion genérica
        crearProducto(tienda, categoria, tipoProducto, "Polo Jersey Premium", "Excelente prenda de vestir");

        // Producto 2: "Casaca Deportiva", descripcion que contiene "polo"
        crearProducto(tienda, categoria, tipoProducto, "Casaca Deportiva", "Ideal para combinar con un polo casual");

        // Producto 3: "Zapatilla Urbana", descripcion sin la palabra "polo"
        crearProducto(tienda, categoria, tipoProducto, "Zapatilla Urbana", "Zapatilla casual para uso diario");
    }

    private void crearProducto(Tienda tienda, Categoria categoria, TipoProducto tipoProducto, String nombre, String descripcion) {
        Producto producto = new Producto();
        producto.setTienda(tienda);
        producto.setCategoria(categoria);
        producto.setTipoProducto(tipoProducto);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecioBase(50.0);
        producto.setActivo(true);
        producto.setEsPersonalizable(false);
        productoRepository.save(producto);
    }

    @Test
    @DisplayName("BUS-001 - Debería buscar productos que contengan la palabra clave en nombre o descripción")
    void buscarProductos_DeberiaRetornarCoincidencias() throws Exception {
        mockMvc.perform(get("/api/v1/productos/buscar")
                .param("q", "polo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Polo Jersey Premium")))
                .andExpect(jsonPath("$[1].nombre", is("Casaca Deportiva")));
    }

    @Test
    @DisplayName("BUS-002 - Debería retornar lista vacía si no hay coincidencias")
    void buscarProductos_DeberiaRetornarListaVacia_CuandoNoHayCoincidencias() throws Exception {
        mockMvc.perform(get("/api/v1/productos/buscar")
                .param("q", "inexistente123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
