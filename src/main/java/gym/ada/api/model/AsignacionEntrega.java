
package gym.ada.api.model;

import gym.ada.api.enums.EstadoEntrega;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "asignaciones_entrega",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_asignacion_pedido",
            columnNames = "pedido_id"
        )
    }
)
public class AsignacionEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "pedido_id",
        nullable = false,
        unique = true
    )
    private Pedido pedido;

    @Column(
        name = "repartidor_uid",
        nullable = false,
        length = 128
    )
    private String repartidorUid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEntrega estado = EstadoEntrega.ASIGNADA;

    @Column(nullable = false)
    private LocalDateTime fechaAsignacion;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaEntrega;

    @Column(name = "ultima_latitud")
    private Double ultimaLatitud;

    @Column(name = "ultima_longitud")
    private Double ultimaLongitud;

    private LocalDateTime fechaUltimaUbicacion;

    @PrePersist
    public void antesDeGuardar() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoEntrega.ASIGNADA;
        }
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Double getUltimaLatitud() {
        return ultimaLatitud;
    }

    public void setUltimaLatitud(Double ultimaLatitud) {
        this.ultimaLatitud = ultimaLatitud;
    }

    public Double getUltimaLongitud() {
        return ultimaLongitud;
    }

    public void setUltimaLongitud(Double ultimaLongitud) {
        this.ultimaLongitud = ultimaLongitud;
    }

    public LocalDateTime getFechaUltimaUbicacion() {
        return fechaUltimaUbicacion;
    }

    public void setFechaUltimaUbicacion(
        LocalDateTime fechaUltimaUbicacion
    ) {
        this.fechaUltimaUbicacion = fechaUltimaUbicacion;
    }
}
