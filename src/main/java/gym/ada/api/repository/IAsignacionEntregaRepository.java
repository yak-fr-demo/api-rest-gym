
package gym.ada.api.repository;

import gym.ada.api.enums.EstadoEntrega;
import gym.ada.api.model.AsignacionEntrega;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IAsignacionEntregaRepository
        extends JpaRepository<AsignacionEntrega, Long> {

    Optional<AsignacionEntrega> findByPedidoId(Long pedidoId);

    boolean existsByPedidoId(Long pedidoId);

    List<AsignacionEntrega>
        findByRepartidorUidOrderByFechaAsignacionDesc(
            String repartidorUid
        );

    List<AsignacionEntrega>
        findByRepartidorUidAndEstado(
            String repartidorUid,
            EstadoEntrega estado
        );
    

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT a
    FROM AsignacionEntrega a
    JOIN FETCH a.pedido
    WHERE a.pedido.id = :pedidoId
    """)
Optional<AsignacionEntrega> bloquearPorPedidoId(
        @Param("pedidoId") Long pedidoId
);

}
