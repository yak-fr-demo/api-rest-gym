
package gym.ada.api.repository;

import gym.ada.api.enums.EstadoEntrega;
import gym.ada.api.model.AsignacionEntrega;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
