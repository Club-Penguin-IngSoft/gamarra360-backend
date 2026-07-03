package pe.com.gamarra360.backend.catalogo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import pe.com.gamarra360.backend.catalogo.entity.Color;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Talla;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.entity.VarianteProducto;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.repository.VarianteProductoRepository;
import pe.com.gamarra360.backend.catalogo.service.impl.VarianteProductoServiceImpl;
import pe.com.gamarra360.backend.exception.RecursoNoEncontradoException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VarianteProductoServiceImplTest {

    @Mock
    private VarianteProductoRepository repository;

    @Mock
    private TiendaRepository tiendaRepository;

    @InjectMocks
    private VarianteProductoServiceImpl service;

    private VarianteProducto varianteExistente;
    private Producto producto;
    private Color color;
    private Talla talla;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setIdProducto(10);
        producto.setIdTienda(5);

        color = new Color();
        color.setIdColor(2);

        talla = new Talla();
        talla.setIdTalla(3);

        varianteExistente = new VarianteProducto();
        varianteExistente.setIdVariante(84);
        varianteExistente.setProducto(producto);
        varianteExistente.setColor(color);
        varianteExistente.setTalla(talla);
        varianteExistente.setSku("SKU-POLO-AZUL-M");
        varianteExistente.setStock(200);
        varianteExistente.setMinimoStock(50);
        varianteExistente.setPrecioAjustado(40.0);
        varianteExistente.setDisponible(true);
        varianteExistente.setImagenUrl("https://cdn.example.com/polo-azul.jpg");
    }

    // ── actualizar (parcial) ──────────────────────────────────────────────────

    @Test
    @DisplayName("debeConservarProductoColorTallaYSku_cuandoSeActualizaSoloPrecioYDisponibilidad")
    void debeConservarProductoColorTallaYSku_cuandoSeActualizaSoloPrecioYDisponibilidad() {
        when(repository.findByIdConColorYTalla(84)).thenReturn(Optional.of(varianteExistente));
        when(repository.save(any(VarianteProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        VarianteProducto request = new VarianteProducto();
        request.setPrecioAjustado(45.0);
        request.setDisponible(true);
        request.setMinimoStock(5);

        VarianteProducto resultado = service.actualizar(84, request);

        assertThat(resultado.getPrecioAjustado()).isEqualTo(45.0);
        assertThat(resultado.getMinimoStock()).isEqualTo(5);
        assertThat(resultado.getDisponible()).isTrue();
        assertThat(resultado.getProducto()).isSameAs(producto);
        assertThat(resultado.getColor()).isSameAs(color);
        assertThat(resultado.getTalla()).isSameAs(talla);
        assertThat(resultado.getSku()).isEqualTo("SKU-POLO-AZUL-M");
        assertThat(resultado.getImagenUrl()).isEqualTo("https://cdn.example.com/polo-azul.jpg");
        assertThat(resultado.getStock()).isEqualTo(200);

        verify(repository).save(varianteExistente);
        verify(repository, never()).existsById(any());
    }

    @Test
    @DisplayName("debeLanzarRecursoNoEncontrado_cuandoIdNoExisteEnActualizar")
    void debeLanzarRecursoNoEncontrado_cuandoIdNoExisteEnActualizar() {
        when(repository.findByIdConColorYTalla(99)).thenReturn(Optional.empty());

        VarianteProducto request = new VarianteProducto();
        request.setPrecioAjustado(45.0);

        assertThatThrownBy(() -> service.actualizar(99, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(repository, never()).save(any());
    }

    // ── eliminarVariante (borrado físico + multi-tenancy) ────────────────────

    @Test
    @DisplayName("debeEliminarVariante_cuandoExisteYPerteneceAlComercianteAutenticado")
    void debeEliminarVariante_cuandoExisteYPerteneceAlComercianteAutenticado() {
        Tienda tienda = new Tienda();
        tienda.setIdTienda(5);
        tienda.setIdComerciante(1);

        when(repository.findById(84)).thenReturn(Optional.of(varianteExistente));
        when(tiendaRepository.findByIdComerciante(1)).thenReturn(Optional.of(tienda));
        doNothing().when(repository).delete(varianteExistente);

        service.eliminarVariante(84, 1);

        verify(repository).delete(varianteExistente);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("debeLanzarRecursoNoEncontrado_cuandoLaVarianteNoExiste")
    void debeLanzarRecursoNoEncontrado_cuandoLaVarianteNoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarVariante(99, 1))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(repository, never()).delete(any());
        verifyNoInteractions(tiendaRepository);
    }

    @Test
    @DisplayName("debeLanzarAccesoNoAutorizado_cuandoLaVarianteEsDeOtroComerciante")
    void debeLanzarAccesoNoAutorizado_cuandoLaVarianteEsDeOtroComerciante() {
        // La variante pertenece a la tienda 5, pero el comerciante 2 tiene la tienda 9
        Tienda tiendaOtroComerciante = new Tienda();
        tiendaOtroComerciante.setIdTienda(9);
        tiendaOtroComerciante.setIdComerciante(2);

        when(repository.findById(84)).thenReturn(Optional.of(varianteExistente));
        when(tiendaRepository.findByIdComerciante(2)).thenReturn(Optional.of(tiendaOtroComerciante));

        assertThatThrownBy(() -> service.eliminarVariante(84, 2))
                .isInstanceOf(AccessDeniedException.class);

        verify(repository, never()).delete(any());
    }
}
