package gym.ada.api.service.implement;

import gym.ada.api.dto.CrearPedidoDto;
import gym.ada.api.dto.pedido.ActualizarEstadoPedidoDto;
import gym.ada.api.dto.pedido.DetallePedidoDto;
import gym.ada.api.dto.pedido.DetallePedidoResponseDto;
import gym.ada.api.dto.pedido.HistorialEstadoResponseDto;
import gym.ada.api.dto.pedido.PedidoResponseDto;
import gym.ada.api.model.DetallePedido;
import gym.ada.api.enums.EstadoPedido;
import gym.ada.api.model.HistorialEstadoPedido;
import gym.ada.api.model.Pedido;
import gym.ada.api.model.Producto;
import gym.ada.api.model.Talla;
import gym.ada.api.repository.IDetallePedidoRepository;
import gym.ada.api.repository.IHistorialEstadoPedidoRepository;
import gym.ada.api.repository.IPedidoRepository;
import gym.ada.api.repository.IProductoRepository;
import gym.ada.api.repository.IProductoTallaRepository;
import gym.ada.api.repository.ITallaRepository;
import gym.ada.api.service.IPedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoServiceImplement implements IPedidoService {

	private final IPedidoRepository pedidoRepository;
	private final IDetallePedidoRepository detallePedidoRepository;
	private final IHistorialEstadoPedidoRepository historialEstadoPedidoRepository;
	private final IProductoRepository productoRepository;
	private final ITallaRepository tallaRepository;
	private final IProductoTallaRepository productoTallaRepository;
	public PedidoServiceImplement(
	        IPedidoRepository pedidoRepository,
	        IDetallePedidoRepository detallePedidoRepository,
	        IHistorialEstadoPedidoRepository historialEstadoPedidoRepository,
	        IProductoRepository productoRepository,
	        ITallaRepository tallaRepository,
	        IProductoTallaRepository productoTallaRepository
	) {

	    this.pedidoRepository = pedidoRepository;
	    this.detallePedidoRepository = detallePedidoRepository;
	    this.historialEstadoPedidoRepository = historialEstadoPedidoRepository;
	    this.productoRepository = productoRepository;
	    this.tallaRepository = tallaRepository;
	    this.productoTallaRepository = productoTallaRepository;
	}

    @Override
    @Transactional
    public PedidoResponseDto crearPedido(CrearPedidoDto dto) {

        Pedido pedido = new Pedido();

        pedido.setFirebaseUid(dto.getFirebaseUid());
        pedido.setDireccionEntrega(dto.getDireccionEntrega());
        pedido.setReferencia(dto.getReferencia());
        pedido.setCostoEnvio(dto.getCostoEnvio());
        pedido.setLatitudEntrega(dto.getLatitudEntrega());
        pedido.setLongitudEntrega(dto.getLongitudEntrega());
     

        pedido.setCostoEnvio(dto.getCostoEnvio());

        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal subtotalPedido = BigDecimal.ZERO;

        List<DetallePedido> detalles = new ArrayList<>();

        for (DetallePedidoDto item : dto.getProductos()) {

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Producto no encontrado: " + item.getProductoId()
                            )
                    );

            if (!producto.isActivo()) {
                throw new RuntimeException(
                        "El producto no está disponible: " + producto.getTitulo()
                );
            }

            Talla talla = tallaRepository.findById(item.getTallaId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Talla no encontrada: " + item.getTallaId()
                            )
                    );
            productoTallaRepository
            .findByProductoIdAndTallaId(
                    item.getProductoId(),
                    item.getTallaId()
            )
            .orElseThrow(() ->
                    new RuntimeException(
                            "La talla seleccionada no pertenece al producto"
                    )
            );

            BigDecimal precioUnitario = producto.getPrecioUnidad();

            BigDecimal subtotalDetalle = precioUnitario.multiply(
                    BigDecimal.valueOf(item.getCantidad())
            );

            DetallePedido detalle = new DetallePedido();

            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setTalla(talla);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotalDetalle);

            detalles.add(detalle);

            subtotalPedido = subtotalPedido.add(subtotalDetalle);
        }

        pedido.setSubtotal(subtotalPedido);

        BigDecimal total = subtotalPedido.add(dto.getCostoEnvio());

        pedido.setTotal(total);

        pedido.setDetalles(detalles);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        HistorialEstadoPedido historial = new HistorialEstadoPedido();

        historial.setPedido(pedidoGuardado);
        historial.setEstado(EstadoPedido.PENDIENTE);
        historial.setComentario("Pedido creado");

        historialEstadoPedidoRepository.save(historial);

        return convertirRespuesta(pedidoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDto> listarPedidosUsuario(String firebaseUid) {

        List<Pedido> pedidos =
                pedidoRepository.findByFirebaseUidOrderByFechaCreacionDesc(firebaseUid);

        return pedidos.stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDto obtenerPedido(Long id, String firebaseUid) {

        Pedido pedido = pedidoRepository
                .findByIdAndFirebaseUid(id, firebaseUid)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado")
                );

        return convertirRespuesta(pedido);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDto> listarTodosPedidos() {

        List<Pedido> pedidos =
                pedidoRepository.findAllByOrderByFechaCreacionDesc();

        return pedidos.stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public void cancelarPedido(Long id, String firebaseUid) {

        Pedido pedido = pedidoRepository
                .findByIdAndFirebaseUid(id, firebaseUid)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado")
                );

        if (
                pedido.getEstado() == EstadoPedido.ENTREGADO ||
                pedido.getEstado() == EstadoPedido.CANCELADO
        ) {
            throw new RuntimeException(
                    "El pedido no puede ser cancelado"
            );
        }

        pedido.setEstado(EstadoPedido.CANCELADO);

        pedidoRepository.save(pedido);

        HistorialEstadoPedido historial = new HistorialEstadoPedido();

        historial.setPedido(pedido);
        historial.setEstado(EstadoPedido.CANCELADO);
        historial.setComentario("Pedido cancelado por el usuario");

        historialEstadoPedidoRepository.save(historial);
    }

    private PedidoResponseDto convertirRespuesta(Pedido pedido) {

        PedidoResponseDto dto = new PedidoResponseDto();

        dto.setId(pedido.getId());
        dto.setFirebaseUid(pedido.getFirebaseUid());
        dto.setEstado(pedido.getEstado());
        dto.setSubtotal(pedido.getSubtotal());
        dto.setCostoEnvio(pedido.getCostoEnvio());
        dto.setTotal(pedido.getTotal());
        dto.setDireccionEntrega(pedido.getDireccionEntrega());
        dto.setReferencia(pedido.getReferencia());
        dto.setFechaCreacion(pedido.getFechaCreacion());

        List<DetallePedidoResponseDto> productos = new ArrayList<>();

        for (DetallePedido detalle : pedido.getDetalles()) {

            DetallePedidoResponseDto productoDto =
                    new DetallePedidoResponseDto();

            productoDto.setProductoId(
                    detalle.getProducto().getId()
            );

            productoDto.setProducto(
                    detalle.getProducto().getTitulo()
            );

            productoDto.setTallaId(
                    detalle.getTalla().getId()
            );

            productoDto.setTalla(
                    detalle.getTalla().getNombre()
            );

            productoDto.setCantidad(
                    detalle.getCantidad()
            );

            productoDto.setPrecioUnitario(
                    detalle.getPrecioUnitario()
            );

            productoDto.setSubtotal(
                    detalle.getSubtotal()
            );

            productos.add(productoDto);
        }

        dto.setProductos(productos);

        return dto;
    }
    
    @Override
    @Transactional
    public void actualizarEstado(
            Long id,
            ActualizarEstadoPedidoDto dto
    ) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado")
                );

        EstadoPedido estadoActual = pedido.getEstado();
        EstadoPedido nuevoEstado = dto.getEstado();

        if (estadoActual == EstadoPedido.CANCELADO) {
            throw new RuntimeException(
                    "No se puede cambiar el estado de un pedido cancelado"
            );
        }

        if (estadoActual == EstadoPedido.ENTREGADO) {
            throw new RuntimeException(
                    "No se puede cambiar el estado de un pedido entregado"
            );
        }

        if (estadoActual == nuevoEstado) {
            throw new RuntimeException(
                    "El pedido ya se encuentra en estado " + nuevoEstado
            );
        }

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);

        HistorialEstadoPedido historial =
                new HistorialEstadoPedido();

        historial.setPedido(pedido);
        historial.setEstado(nuevoEstado);
        historial.setComentario(dto.getComentario());

        historialEstadoPedidoRepository.save(historial);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoResponseDto> obtenerHistorial(
            Long id,
            String firebaseUid
    ) {

        Pedido pedido = pedidoRepository
                .findByIdAndFirebaseUid(id, firebaseUid)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado")
                );

        return historialEstadoPedidoRepository
                .findByPedidoOrderByFechaAsc(pedido)
                .stream()
                .map(historial -> {

                    HistorialEstadoResponseDto dto =
                            new HistorialEstadoResponseDto();

                    dto.setEstado(historial.getEstado());
                    dto.setFecha(historial.getFecha());
                    dto.setComentario(historial.getComentario());

                    return dto;
                })
                .toList();
    }
    
    
}