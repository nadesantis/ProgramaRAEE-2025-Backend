package com.products.orders.web;

import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;
import com.products.orders.application.*;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;
import com.products.orders.domain.OrderStatus;
import com.products.orders.web.dto.CreateOrderRequest;
import com.products.orders.web.dto.OrderItemDTO;
import com.products.orders.web.dto.OrderResponse;

import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final CreateOrderUseCase createUC;
  private final ApproveOrderUseCase approveUC;
  private final GetOrderUseCase getUC;
  private final ListOrdersUseCase listUC;
  private final ClientRepository clientRepo;
  private final AuditService audit;

  public OrderController(CreateOrderUseCase createUC,
                         ApproveOrderUseCase approveUC,
                         GetOrderUseCase getUC,
                         ListOrdersUseCase listUC,
                         ClientRepository clientRepo,
                         AuditService audit) {
    this.createUC = createUC;
    this.approveUC = approveUC;
    this.getUC = getUC;
    this.listUC = listUC;
    this.clientRepo = clientRepo;
    this.audit = audit;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS')")
  public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
    var items = req.getItems().stream()
        .map(i -> new CreateOrderUseCase.ItemInput(i.getProductId(), i.getQuantity()))
        .toList();

    Order o = createUC.handle(req.getClientId(), items);

    String clientName = (o.getClientId() == null) ? null
        : clientRepo.findById(o.getClientId()).map(Client::getName).orElse(null);

    audit.success(AuditAction.ORDER_CREATE, "Order", o.getId(), "Cliente #" + o.getClientId());
    return ResponseEntity.ok(toResponse(o, clientName));
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS')")
  public ResponseEntity<OrderResponse> approve(@PathVariable Long id) {
    Order o = approveUC.handle(id);

    String clientName = (o.getClientId() == null) ? null
        : clientRepo.findById(o.getClientId()).map(Client::getName).orElse(null);

    audit.success(AuditAction.ORDER_APPROVE, "Order", o.getId(), "Aprobada");
    return ResponseEntity.ok(toResponse(o, clientName));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS','OPERADOR_VENTAS')")
  public ResponseEntity<OrderResponse> get(@PathVariable Long id) {
    return getUC.handle(id)
        .map(o -> {
          String name = (o.getClientId() == null) ? null
              : clientRepo.findById(o.getClientId()).map(Client::getName).orElse(null);
          audit.success(AuditAction.ORDER_READ, "Order", id, "Detalle");
          return ResponseEntity.ok(toResponse(o, name));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS','OPERADOR_VENTAS')")
  public Page<OrderResponse> list(@RequestParam(required = false) Long clientId,
                                  @RequestParam(required = false) OrderStatus status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var p = listUC.handle(clientId, status, pageable);

    Map<Long, String> nameCache = new HashMap<>();
    var mapped = p.map(o -> {
      String name = null;
      if (o.getClientId() != null) {
        name = nameCache.computeIfAbsent(
            o.getClientId(),
            id -> clientRepo.findById(id).map(Client::getName).orElse(null)
        );
      }
      return toResponse(o, name);
    });

    audit.success(AuditAction.ORDER_LIST, "Listado órdenes");
    return mapped;
  }

  private OrderResponse toResponse(Order o, String clientName) {
    OrderResponse r = new OrderResponse();
    r.setId(o.getId());
    r.setClientId(o.getClientId());
    r.setClientName(clientName);
    r.setStatus(o.getStatus());
    r.setTotalAmount(o.getTotalAmount());
    r.setCreatedAt(o.getCreatedAt());
    r.setApprovedAt(o.getApprovedAt());
    var items = o.getItems().stream().map(this::toDto).toList();
    r.setItems(items);
    return r;
  }

  private OrderItemDTO toDto(OrderItem it) {
    OrderItemDTO dto = new OrderItemDTO();
    dto.setId(it.getId());
    dto.setProductId(it.getProductId());
    dto.setProductName(it.getProductName());
    dto.setUnitPrice(it.getUnitPrice());
    dto.setQuantity(it.getQuantity());
    dto.setLineTotal(it.getLineTotal());
    return dto;
  }
}
