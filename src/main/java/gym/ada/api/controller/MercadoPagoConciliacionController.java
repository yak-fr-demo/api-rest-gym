package gym.ada.api.controller;

import gym.ada.api.model.Pedido;
import gym.ada.api.repository.IPedidoRepository;
import gym.ada.api.service.implement.MercadoPagoWebhookService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoConciliacionController {

    private final MercadoPagoWebhookService mercadoPagoService;
    private final IPedidoRepository pedidoRepository;

    public MercadoPagoConciliacionController(
            MercadoPagoWebhookService mercadoPagoService,
            IPedidoRepository pedidoRepository
    ) {
        this.mercadoPagoService = mercadoPagoService;
        this.pedidoRepository = pedidoRepository;
    }

    @PostMapping("/conciliar/{pedidoId}")
    public ResponseEntity<Map<String, Object>> conciliar(
            @PathVariable Long pedidoId
    ) {

        // 1. Verificar que exista el pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        // 2. Consultar Mercado Pago y verificar el pago
        String resultado = mercadoPagoService.conciliarPedido(pedidoId);

        // 3. Consultar el estado actualizado
        Pedido actualizado = pedidoRepository.findById(pedidoId)
                .orElseThrow();

        // 4. Devolver resultado
        return ResponseEntity.ok(Map.of(
                "pedidoId", pedidoId,
                "estado", actualizado.getEstado().name(),
                "resultado", resultado
        ));
    }
}