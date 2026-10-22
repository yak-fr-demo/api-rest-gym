
package gym.ada.api.service;

import gym.ada.api.dto.entrega.AsignacionEntregaResponseDto;

import java.util.List;

public interface IEntregaService {

    AsignacionEntregaResponseDto asignarEntrega(
            Long pedidoId,
            String repartidorUid
    );

    List<AsignacionEntregaResponseDto> listarPorRepartidor(
            String repartidorUid
    );

    AsignacionEntregaResponseDto obtenerPorPedido(
            Long pedidoId
    );
}
