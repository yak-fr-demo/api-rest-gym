
package gym.ada.api.controller;

import gym.ada.api.dto.entrega.AsignarEntregaDto;
import gym.ada.api.dto.entrega.AsignacionEntregaResponseDto;
import gym.ada.api.service.IEntregaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final IEntregaService entregaService;
    private final String adminUids;

    public EntregaController(
            IEntregaService entregaService,
            @Value("${gyselmood.admin-uids:}") String adminUids
    ) {
        this.entregaService = entregaService;
        this.adminUids = adminUids;
    }

    // Identidad validada por el filtro Firebase existente.
    private String obtenerUid(HttpServletRequest request) {

        Object valor = request.getAttribute("firebaseUid");

        if (!(valor instanceof String uid) || uid.isBlank()) {
            throw new AccesoDenegado(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

        return uid;
    }

    // Lista pequeña de administradores autorizados.
    // Se configura desde Render, sin modificar filtros.
    private void exigirAdmin(HttpServletRequest request) {

        String uid = obtenerUid(request);

        boolean esAdmin = Arrays.stream(adminUids.split(","))
                .map(String::trim)
                .anyMatch(uid::equals);

        if (!esAdmin) {
            throw new AccesoDenegado(
                    HttpStatus.FORBIDDEN,
                    "Solo un administrador puede realizar esta acción"
            );
        }
    }

    // ADMIN: asignar pedido a un repartidor.
    @PostMapping("/pedido/{pedidoId}/asignar")
    public ResponseEntity<AsignacionEntregaResponseDto> asignar(
            @PathVariable Long pedidoId,
            @Valid @RequestBody AsignarEntregaDto dto,
            HttpServletRequest request
    ) {

        exigirAdmin(request);

        AsignacionEntregaResponseDto resultado =
                entregaService.asignarEntrega(
                        pedidoId,
                        dto.getRepartidorUid()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resultado);
    }

    // REPARTIDOR: únicamente sus propias entregas.
    @GetMapping("/mis")
    public ResponseEntity<List<AsignacionEntregaResponseDto>>
    misEntregas(HttpServletRequest request) {

        String uid = obtenerUid(request);

        return ResponseEntity.ok(
                entregaService.listarPorRepartidor(uid)
        );
    }

    // ADMIN: consultar asignación de un pedido.
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<AsignacionEntregaResponseDto>
    obtenerAsignacion(
            @PathVariable Long pedidoId,
            HttpServletRequest request
    ) {

        exigirAdmin(request);

        return ResponseEntity.ok(
                entregaService.obtenerPorPedido(pedidoId)
        );
    }

    @ExceptionHandler(AccesoDenegado.class)
    public ResponseEntity<Map<String, String>>
    manejarAccesoDenegado(AccesoDenegado e) {

        return ResponseEntity.status(e.estado)
                .body(Map.of("error", e.getMessage()));
    }

    private static class AccesoDenegado
            extends RuntimeException {

        private final HttpStatus estado;

        AccesoDenegado(HttpStatus estado, String mensaje) {
            super(mensaje);
            this.estado = estado;
        }
    }
}
