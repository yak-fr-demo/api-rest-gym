package gym.ada.api.service;

import gym.ada.api.dto.entrega.SeguimientoEntregaDto;
import gym.ada.api.dto.entrega.UbicacionEntregaDto;

public interface IEntregaMovimientoService {

    void iniciar(Long pedidoId, String uid);

    void actualizarUbicacion(Long pedidoId, String uid, UbicacionEntregaDto dto);

    SeguimientoEntregaDto obtenerSeguimiento(Long pedidoId, String uidCliente);

    void finalizar(Long pedidoId, String uid);
}