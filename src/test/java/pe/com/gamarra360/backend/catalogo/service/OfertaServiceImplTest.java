package pe.com.gamarra360.backend.catalogo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.gamarra360.backend.catalogo.dto.OfertaRequestDto;
import pe.com.gamarra360.backend.catalogo.dto.OfertaResponseDto;
import pe.com.gamarra360.backend.catalogo.entity.Oferta;
import pe.com.gamarra360.backend.catalogo.entity.Producto;
import pe.com.gamarra360.backend.catalogo.entity.Tienda;
import pe.com.gamarra360.backend.catalogo.repository.OfertaRepository;
import pe.com.gamarra360.backend.catalogo.repository.ProductoRepository;
import pe.com.gamarra360.backend.catalogo.repository.TiendaRepository;
import pe.com.gamarra360.backend.catalogo.service.impl.OfertaServiceImpl;
import pe.com.gamarra360.backend.enums.TipoDescuento;
import pe.com.gamarra360.backend.exception.DatosInvalidosException;
import pe.com.gamarra360.backend.exception.OfertaConflictoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfertaServiceImplTest {

    @Mock private OfertaRepository ofertaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private TiendaRepository tiendaRepository;

    private OfertaServiceImpl service;

    private static final int COMERCIANTE_ID = 1;
    private static final int ID_TIENDA = 5;
    private static final int ID_PRODUCTO = 10;

    private Tienda tienda;
    private Producto producto;
    private OfertaRequestDto request;

    @BeforeEach
    void setUp() {
        service = new OfertaServiceImpl(ofertaRepository, productoRepository, tiendaRepository);

        tienda = new Tienda();
        tienda.setIdTienda(ID_TIENDA);
        tienda.setIdComerciante(COMERCIANTE_ID);

        producto = new Producto();
        producto.setIdProducto(ID_PRODUCTO);
        producto.setIdTienda(ID_TIENDA);
        producto.setNombre("Polo Pima Hombre");
        producto.setActivo(true);

        request = new OfertaRequestDto();
        request.setTitulo("Cyber Days");
        request.setTipoDescuento(TipoDescuento.PORCENTAJE);
        request.setValorDescuento(20.0);
        request.setFechaInicio(LocalDate.now());
        request.setFechaFin(LocalDate.now().plusDays(5));
        request.setActiva(true);
        request.setIdsProductos(List.of(ID_PRODUCTO));

        when(tiendaRepository.findByIdComerciante(COMERCIANTE_ID)).thenReturn(Optional.of(tienda));
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(inv -> {
            Oferta o = inv.getArgument(0);
            if (o.getIdOferta() == null) o.setIdOferta(99);
            return o;
        });
    }

    @Test
    void debeCrearOferta_cuandoNoHayProductosEnConflicto() {
        when(productoRepository.findByIdProductoInAndIdTiendaAndActivoTrue(anyList(), anyInt()))
                .thenReturn(List.of(producto));
        when(productoRepository.findByOferta_IdOferta(anyInt())).thenReturn(List.of(producto));

        OfertaResponseDto response = service.crear(request, COMERCIANTE_ID);

        assertThat(response.getIdsProductos()).containsExactly(ID_PRODUCTO);
        ArgumentCaptor<List<Producto>> captor = ArgumentCaptor.forClass(List.class);
        verify(productoRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Producto::getOferta).isNotNull();
    }

    @Test
    void debeLanzarDatosInvalidos_cuandoNoSeSeleccionanProductos() {
        request.setIdsProductos(Collections.emptyList());

        assertThatThrownBy(() -> service.crear(request, COMERCIANTE_ID))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("al menos un producto");

        verify(productoRepository, never()).saveAll(any());
    }

    @Test
    void debeLanzarDatosInvalidos_cuandoNingunProductoSeleccionadoEstaPublicado() {
        when(productoRepository.findByIdProductoInAndIdTiendaAndActivoTrue(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.crear(request, COMERCIANTE_ID))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("publicado");

        verify(productoRepository, never()).saveAll(any());
    }

    @Test
    void debeLanzarConflicto_cuandoProductoYaTieneOfertaActivaVigente() {
        Oferta ofertaAnterior = new Oferta();
        ofertaAnterior.setIdOferta(50);
        ofertaAnterior.setTitulo("Oferta Verano");
        ofertaAnterior.setActiva(true);
        ofertaAnterior.setFechaInicio(LocalDateTime.now().minusDays(1));
        ofertaAnterior.setFechaFin(LocalDateTime.now().plusDays(10));
        producto.setOferta(ofertaAnterior);

        when(productoRepository.findByIdProductoInAndIdTiendaAndActivoTrue(anyList(), anyInt()))
                .thenReturn(List.of(producto));

        assertThatThrownBy(() -> service.crear(request, COMERCIANTE_ID))
                .isInstanceOf(OfertaConflictoException.class)
                .satisfies(ex -> {
                    OfertaConflictoException conflicto = (OfertaConflictoException) ex;
                    assertThat(conflicto.getConflictos()).hasSize(1);
                    assertThat(conflicto.getConflictos().get(0).getIdOferta()).isEqualTo(50);
                    assertThat(conflicto.getConflictos().get(0).getProductosEnConflicto())
                            .extracting("idProducto")
                            .containsExactly(ID_PRODUCTO);
                });

        verify(productoRepository, never()).saveAll(any());
    }

    @Test
    void debeSobrescribirAsignacion_cuandoSeConfirmaForzarSobrescritura() {
        Oferta ofertaAnterior = new Oferta();
        ofertaAnterior.setIdOferta(50);
        ofertaAnterior.setTitulo("Oferta Verano");
        ofertaAnterior.setActiva(true);
        ofertaAnterior.setFechaInicio(LocalDateTime.now().minusDays(1));
        ofertaAnterior.setFechaFin(LocalDateTime.now().plusDays(10));
        producto.setOferta(ofertaAnterior);
        request.setForzarSobrescritura(true);

        when(productoRepository.findByIdProductoInAndIdTiendaAndActivoTrue(anyList(), anyInt()))
                .thenReturn(List.of(producto));
        when(productoRepository.findByOferta_IdOferta(anyInt())).thenReturn(List.of(producto));

        OfertaResponseDto response = service.crear(request, COMERCIANTE_ID);

        assertThat(response).isNotNull();
        ArgumentCaptor<List<Producto>> captor = ArgumentCaptor.forClass(List.class);
        verify(productoRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getOferta().getIdOferta()).isEqualTo(99);
    }
}
