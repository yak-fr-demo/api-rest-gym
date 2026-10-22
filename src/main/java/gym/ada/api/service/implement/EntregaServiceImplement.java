
package gym.ada.api.service.implement;

import gym.ada.api.dto.entrega.AsignacionEntregaResponseDto;
import gym.ada.api.enums.EstadoEntrega;
import gym.ada.api.enums.EstadoPedido;
import gym.ada.api.model.AsignacionEntrega;
import gym.ada.api.model.Pedido;
import gym.ada.api.repository.IAsignacionEntregaRepository;
import gym.ada.api.repository.IPedidoRepository;
import gym.ada.api.service.IEntregaService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class EntregaServiceImplement implements IEntregaService {

    private final IAsignacionEntregaRepository asignacionRepository;
    private final IPedidoRepository pedidoRepository;

    public EntregaServiceImplement(
            IAsignacionEntregaRepository asignacionRepository,
            IPedidoRepository pedidoRepository
    ) {
        this.asignacionRepository = asignacionRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional
    public AsignacionEntregaResponseDto asignarEntrega(
            Long pedidoId,
            String repartidorUid
    ) {

        if (repartidorUid == null ||
                repartidorUid.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un repartidor"
            );
        }

        if (repartidorUid.length() > 128) {
            throw new IllegalArgumentException(
                    "El UID del repartidor es demasiado largo"
            );
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pedido no encontrado"
                        )
                );

        EstadoPedido estado = pedido.getEstado();

        // No asignar pedidos pendientes de pago,
        // cancelados o entregados.
        if (estado != EstadoPedido.CONFIRMADO &&
                estado != EstadoPedido.PREPARANDO) {

            throw new IllegalStateException(
                    "El pedido debe estar CONFIRMADO " +
                    "o PREPARANDO para asignar un repartidor"
            );
        }

        if (asignacionRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException(
                    "El pedido ya tiene un repartidor asignado"
            );
        }

        AsignacionEntrega asignacion =
                new AsignacionEntrega();

        asignacion.setPedido(pedido);
        asignacion.setRepartidorUid(repartidorUid.trim());
        asignacion.setEstado(EstadoEntrega.ASIGNADA);

        AsignacionEntrega guardada =
                asignacionRepository.saveAndFlush(asignacion);

        return convertirRespuesta(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEntregaResponseDto> listarPorRepartidor(
            String repartidorUid
    ) {

        return asignacionRepository
                .findByRepartidorUidOrderByFechaAsignacionDesc(
                        repartidorUid
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }


@Override
@Transactional(readOnly = true)
public AsignacionEntregaResponseDto obtenerPorPedido(
        Long pedidoId
) {
    AsignacionEntrega asignacion =
            asignacionRepository.findByPedidoId(pedidoId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Este pedido no tiene entrega asignada"
                            )
                    );

    return convertirRespuesta(asignacion);
}


    private AsignacionEntregaResponseDto convertirRespuesta(
            AsignacionEntrega asignacion
    ) {

        Pedido pedido = asignacion.getPedido();

        AsignacionEntregaResponseDto dto =
                new AsignacionEntregaResponseDto();

        dto.setId(asignacion.getId());
        dto.setPedidoId(pedido.getId());
        dto.setRepartidorUid(
                asignacion.getRepartidorUid()
        );
        dto.setEstado(asignacion.getEstado());

        dto.setDireccionEntrega(
                pedido.getDireccionEntrega()
        );

        dto.setReferencia(pedido.getReferencia());

        dto.setLatitudEntrega(
                pedido.getLatitudEntrega()
        );

        dto.setLongitudEntrega(
                pedido.getLongitudEntrega()
        );

        dto.setFechaAsignacion(
                asignacion.getFechaAsignacion()
        );

        return dto;
    }
}
