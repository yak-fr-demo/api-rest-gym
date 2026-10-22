
package gym.ada.api.dto.entrega;

import gym.ada.api.enums.EstadoEntrega;
import java.time.LocalDateTime;

public class AsignacionEntregaResponseDto {

    private Long id;
    private Long pedidoId;
    private String repartidorUid;
    private EstadoEntrega estado;
    private String direccionEntrega;
    private String referencia;
    private Double latitudEntrega;
    private Double longitudEntrega;
    private LocalDateTime fechaAsignacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getRepartidorUid() {
        return repartidorUid;
    }

    public void setRepartidorUid(String repartidorUid) {
        this.repartidorUid = repartidorUid;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntrega estado) {
        this.estado = estado;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Double getLatitudEntrega() {
        return latitudEntrega;
    }

    public void setLatitudEntrega(Double latitudEntrega) {
        this.latitudEntrega = latitudEntrega;
    }

    public Double getLongitudEntrega() {
        return longitudEntrega;
    }

    public void setLongitudEntrega(Double longitudEntrega) {
        this.longitudEntrega = longitudEntrega;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(
            LocalDateTime fechaAsignacion
    ) {
        this.fechaAsignacion = fechaAsignacion;
    }
}
