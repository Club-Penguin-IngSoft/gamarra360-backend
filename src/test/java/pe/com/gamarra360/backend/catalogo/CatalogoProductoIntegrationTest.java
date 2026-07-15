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
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.CategoriaRepository;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.repository.TipoProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.enums.RolEnum;
import pe.com.gamarra360.backend.usuario.entity.Comerciante;
import pe.com.gamarra360.backend.usuario.repository.ComercianteRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CatalogoProductoIntegrationTest extends IntegrationTestBase {

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

    @Autowired
    private VarianteProductoRepository varianteProductoRepository;

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
        comerciante.setEmail("comerciante.verificado@gamarra360.com");
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

        // 3. Dos Categorias: "Polo" y "Casacas"
        Categoria catPolo = new Categoria();
        catPolo.setNombreCategoria("Polo");
        catPolo.setDescripcion("Polos de todo tipo");
        catPolo = categoriaRepository.save(catPolo);

        Categoria catCasacas = new Categoria();
        catCasacas.setNombreCategoria("Casacas");
        catCasacas.setDescripcion("Casacas de todo tipo");
        catCasacas = categoriaRepository.save(catCasacas);

        // 4. Un TipoProducto válido
        TipoProducto tipoPolo = new TipoProducto();
        tipoPolo.setNombre("Polo de Algodón");
        tipoPolo.setCategoria(catPolo);
        tipoPolo = tipoProductoRepository.save(tipoPolo);

        // 5. 4 Productos activos en categoria "Polo" con sus respectivas VarianteProducto
        // Producto 1: S/15 (fuera de rango)
        crearProductoConVariante(tienda, catPolo, tipoPolo, "Polo 15", 15.0);

        // Producto 2: S/25 (dentro de rango)
        crearProductoConVariante(tienda, catPolo, tipoPolo, "Polo 25", 25.0);

        // Producto 3: S/70 (dentro de rango)
        crearProductoConVariante(tienda, catPolo, tipoPolo, "Polo 70", 70.0);

        // Producto 4: S/100 (fuera de rango)
        crearProductoConVariante(tienda, catPolo, tipoPolo, "Polo 100", 100.0);
    }

    private void crearProductoConVariante(Tienda tienda, Categoria categoria, TipoProducto tipoProducto, String nombre, Double precio) {
        Producto producto = new Producto();
        producto.setTienda(tienda);
        producto.setCategoria(categoria);
        producto.setTipoProducto(tipoProducto);
        producto.setNombre(nombre);
        producto.setDescripcion("Descripcion de " + nombre);
        producto.setPrecioBase(precio);
        producto.setActivo(true);
        producto.setEsPersonalizable(false);
        producto = productoRepository.save(producto);

        VarianteProducto variante = new VarianteProducto();
        variante.setProducto(producto);
        variante.setSku("SKU-" + nombre.replace(" ", "-").toUpperCase());
        variante.setStock(10);
        variante.setMinimoStock(2);
        variante.setDisponible(true);
        varianteProductoRepository.save(variante);
    }

    @Test
    @DisplayName("CAT-001 - Debería filtrar productos por categoría, rango de precios y ordenar ascendentemente")
    void filtrarYOrdenarProductos_DeberiaRetornarSoloDentroDeRangoOrdenados() throws Exception {
        mockMvc.perform(get("/api/v1/productos")
                .param("categorias", "Polo")
                .param("precioMin", "20")
                .param("precioMax", "80")
                .param("sort", "PRICE_ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(2)))
                .andExpect(jsonPath("$.contenido[0].nombre", is("Polo 25")))
                .andExpect(jsonPath("$.contenido[0].precioBase", is(25.0)))
                .andExpect(jsonPath("$.contenido[1].nombre", is("Polo 70")))
                .andExpect(jsonPath("$.contenido[1].precioBase", is(70.0)));
    }
}
