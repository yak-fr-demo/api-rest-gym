package gym.ada.api.service.implement;

import gym.ada.api.dto.entrega.SeguimientoEntregaDto;
import gym.ada.api.dto.entrega.UbicacionEntregaDto;
import gym.ada.api.enums.EstadoEntrega;
import gym.ada.api.enums.EstadoPedido;
import gym.ada.api.model.AsignacionEntrega;
import gym.ada.api.model.HistorialEstadoPedido;
import gym.ada.api.model.Pedido;
import gym.ada.api.repository.IAsignacionEntregaRepository;
import gym.ada.api.repository.IHistorialEstadoPedidoRepository;
import gym.ada.api.repository.IPedidoRepository;
import gym.ada.api.service.IEntregaMovimientoService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class EntregaMovimientoServiceImplement implements IEntregaMovimientoService {

    private final IAsignacionEntregaRepository entregaRepository;
    private final IPedidoRepository pedidoRepository;
    private final IHistorialEstadoPedidoRepository historialRepository;

    public EntregaMovimientoServiceImplement(
            IAsignacionEntregaRepository entregaRepository,
            IPedidoRepository pedidoRepository,
            IHistorialEstadoPedidoRepository historialRepository
    ) {
        this.entregaRepository = entregaRepository;
        this.pedidoRepository = pedidoRepository;
        this.historialRepository = historialRepository;
    }

    // REPARTIDOR: comienza el reparto.
    @Override
    @Transactional
    public void iniciar(Long pedidoId, String uid) {
        AsignacionEntrega entrega = buscarEntregaBloqueada(pedidoId, uid);

        if (entrega.getEstado() != EstadoEntrega.ASIGNADA) {
            throw conflicto("La entrega no está ASIGNADA");
        }

        Pedido pedido = entrega.getPedido();

        if (pedido.getEstado() != EstadoPedido.CONFIRMADO
                && pedido.getEstado() != EstadoPedido.PREPARANDO
                && pedido.getEstado() != EstadoPedido.ENVIADO) {
            throw conflicto("El pedido no está listo para reparto");
        }

        LocalDateTime ahora = LocalDateTime.now();

        entrega.setEstado(EstadoEntrega.EN_CAMINO);
        entrega.setFechaInicio(ahora);

        pedido.setEstado(EstadoPedido.EN_CAMINO);

        entregaRepository.save(entrega);
        pedidoRepository.save(pedido);

        registrarHistorial(
                pedido,
                EstadoPedido.EN_CAMINO,
                "El repartidor inició la entrega"
        );
    }

    // REPARTIDOR: envía su ubicación.
    @Override
    @Transactional
    public void actualizarUbicacion(
            Long pedidoId,
            String uid,
            UbicacionEntregaDto dto
    ) {
        AsignacionEntrega entrega = buscarEntregaBloqueada(pedidoId, uid);

        if (entrega.getEstado() != EstadoEntrega.EN_CAMINO
                || entrega.getPedido().getEstado() != EstadoPedido.EN_CAMINO) {
            throw conflicto("Solo puedes actualizar GPS durante el reparto");
        }

        Double lat = dto.getLatitud();
        Double lng = dto.getLongitud();

        if (lat == null || lng == null
                || !Double.isFinite(lat)
                || !Double.isFinite(lng)
                || lat < -90 || lat > 90
                || lng < -180 || lng > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coordenadas inválidas"
            );
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime anterior = entrega.getFechaUltimaUbicacion();

        if (anterior != null
                && Duration.between(anterior, ahora).toMillis() < 3000) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Espera al menos 3 segundos"
            );
        }

        entrega.setUltimaLatitud(lat);
        entrega.setUltimaLongitud(lng);
        entrega.setFechaUltimaUbicacion(ahora);

        entregaRepository.save(entrega);
    }

    // CLIENTE: consulta su propio pedido.
    @Override
    @Transactional(readOnly = true)
    public SeguimientoEntregaDto obtenerSeguimiento(
            Long pedidoId,
            String uidCliente
    ) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Pedido no encontrado"
                        )
                );

        if (!uidCliente.equals(pedido.getFirebaseUid())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pedido no encontrado"
            );
        }

        AsignacionEntrega entrega = entregaRepository
                .findByPedidoId(pedidoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El pedido todavía no tiene entrega"
                        )
                );

        Double latitud = null;
        Double longitud = null;
        LocalDateTime actualizacion = null;

        if (entrega.getEstado() == EstadoEntrega.EN_CAMINO
                && pedido.getEstado() == EstadoPedido.EN_CAMINO) {

            LocalDateTime ultima = entrega.getFechaUltimaUbicacion();

            if (ultima != null
                    && !ultima.isAfter(LocalDateTime.now())
                    && Duration.between(ultima, LocalDateTime.now()).toSeconds() <= 120) {

                latitud = entrega.getUltimaLatitud();
                longitud = entrega.getUltimaLongitud();
                actualizacion = ultima;
            }
        }

        return new SeguimientoEntregaDto(
                pedido.getId(),
                pedido.getEstado(),
                entrega.getEstado(),
                latitud,
                longitud,
                actualizacion
        );
    }

    // REPARTIDOR: termina la entrega.
    @Override
    @Transactional
    public void finalizar(Long pedidoId, String uid) {
        AsignacionEntrega entrega = buscarEntregaBloqueada(pedidoId, uid);

        if (entrega.getEstado() != EstadoEntrega.EN_CAMINO) {
            throw conflicto("La entrega no está EN_CAMINO");
        }

        Pedido pedido = entrega.getPedido();

        if (pedido.getEstado() != EstadoPedido.EN_CAMINO) {
            throw conflicto("El pedido no está EN_CAMINO");
        }

        entrega.setEstado(EstadoEntrega.ENTREGADA);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setUltimaLatitud(null);
        entrega.setUltimaLongitud(null);
        entrega.setFechaUltimaUbicacion(null);

        pedido.setEstado(EstadoPedido.ENTREGADO);

        entregaRepository.save(entrega);
        pedidoRepository.save(pedido);

        registrarHistorial(
                pedido,
                EstadoPedido.ENTREGADO,
                "Entrega completada por el repartidor"
        );
    }

    private AsignacionEntrega buscarEntregaBloqueada(Long pedidoId, String uid) {
        AsignacionEntrega entrega = entregaRepository
                .bloquearPorPedidoId(pedidoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Entrega no encontrada"
                        )
                );

        if (!uid.equals(entrega.getRepartidorUid())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No eres el repartidor asignado"
            );
        }

        return entrega;
    }

    private void registrarHistorial(
            Pedido pedido,
            EstadoPedido nuevoEstado,
            String comentario
    ) {
        HistorialEstadoPedido historial = new HistorialEstadoPedido();
        historial.setPedido(pedido);
        historial.setEstado(nuevoEstado);
        historial.setComentario(comentario);

        historialRepository.save(historial);
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                mensaje
        );
    }
}