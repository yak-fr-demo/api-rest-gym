
package gym.ada.api.controller;

import gym.ada.api.dto.entrega.SeguimientoEntregaDto;
import gym.ada.api.dto.entrega.UbicacionEntregaDto;
import gym.ada.api.service.IEntregaMovimientoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/entregas")
public class EntregaMovimientoController {

    private final IEntregaMovimientoService service;

    public EntregaMovimientoController(
            IEntregaMovimientoService service
    ) {
        this.service = service;
    }

    private String obtenerUid(HttpServletRequest request) {

        Object valor = request.getAttribute("firebaseUid");

        if (!(valor instanceof String uid) || uid.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión con Firebase"
            );
        }

        return uid;
    }

    // REPARTIDOR
    @PutMapping("/pedido/{pedidoId}/iniciar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void iniciar(
            @PathVariable Long pedidoId,
            HttpServletRequest request
    ) {
        service.iniciar(
                pedidoId,
                obtenerUid(request)
        );
    }

    // REPARTIDOR
    @PutMapping("/pedido/{pedidoId}/ubicacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizarUbicacion(
            @PathVariable Long pedidoId,
            @Valid @RequestBody UbicacionEntregaDto dto,
            HttpServletRequest request
    ) {
        service.actualizarUbicacion(
                pedidoId,
                obtenerUid(request),
                dto
        );
    }

    // CLIENTE PROPIETARIO DEL PEDIDO
    @GetMapping("/pedido/{pedidoId}/seguimiento")
    public SeguimientoEntregaDto seguimiento(
            @PathVariable Long pedidoId,
            HttpServletRequest request
    ) {
        return service.obtenerSeguimiento(
                pedidoId,
                obtenerUid(request)
        );
    }

    // REPARTIDOR
    @PutMapping("/pedido/{pedidoId}/finalizar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizar(
            @PathVariable Long pedidoId,
            HttpServletRequest request
    ) {
        service.finalizar(
                pedidoId,
                obtenerUid(request)
        );
    }
}
