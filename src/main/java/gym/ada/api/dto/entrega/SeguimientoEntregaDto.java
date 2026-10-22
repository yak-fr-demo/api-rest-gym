package gym.ada.api.dto.entrega;

import gym.ada.api.enums.EstadoEntrega;
import gym.ada.api.enums.EstadoPedido;

import java.time.LocalDateTime;

public class SeguimientoEntregaDto {

    private Long pedidoId;
    private EstadoPedido estadoPedido;
    private EstadoEntrega estadoEntrega;
    private Double latitud;
    private Double longitud;
    private LocalDateTime ultimaActualizacion;

    public SeguimientoEntregaDto(
            Long pedidoId,
            EstadoPedido estadoPedido,
            EstadoEntrega estadoEntrega,
            Double latitud,
            Double longitud,
            LocalDateTime ultimaActualizacion
    ) {
        this.pedidoId = pedidoId;
        this.estadoPedido = estadoPedido;
        this.estadoEntrega = estadoEntrega;
        this.latitud = latitud;
        this.longitud = longitud;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public EstadoPedido getEstadoPedido() {
        return estadoPedido;
    }

    public EstadoEntrega getEstadoEntrega() {
        return estadoEntrega;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }
}
