// src/main/java/com/products/orders/web/OrderController.java
package com.products.orders.web;

import com.products.clients.domain.ClientRepository;
import com.products.orders.application.ListSalesOrdersService;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;
import com.products.orders.domain.OrderRepository;
import com.products.orders.domain.OrderStatus;
import com.products.orders.web.dto.OrderItemDTO;
import com.products.orders.web.dto.OrderResponse;
import com.products.orders.web.dto.OrderSummaryDTO;
import com.products.payments.application.MercadoPagoPreferenceService;
import com.products.pickups.web.dto.MpPreferenceResponse;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.products.backup.application.OrderPdfService;
import com.products.catalog.domain.ProductRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','VENTAS','OPERADOR_VENTAS','ADMIN_VENTAS')")
public class OrderController {

	@Autowired
  private ProductRepository productRepo;
	@Autowired
  private OrderRepository orderRepo;
	@Autowired
  private ClientRepository clientRepo;
	@Autowired
  private ListSalesOrdersService listService;
	@Autowired
  private AuditService audit; 
	@Autowired
  private OrderPdfService orderPdf;
	@Autowired
  private MercadoPagoPreferenceService mercadoPagoPreferenceService;

  private static final Logger log = LoggerFactory.getLogger(OrderController.class);
  /*
  public OrderController(
       OrderRepository orderRepo,
       ClientRepository clientRepo,
       ProductRepository productRepo,
       AuditService audit,
       MercadoPagoPreferenceService mercadoPagoPreferenceService
) {
   this.orderRepo = orderRepo;
   this.clientRepo = clientRepo;
   this.productRepo = productRepo;
   this.audit = audit;
   this.mercadoPagoPreferenceService = mercadoPagoPreferenceService;
}
*/
  
  
  
  
  
  
  
  
  
  
//inyectá el servicio de MP y el repo de órdenes en el controller



//...

//CREAR PREFERENCIA DE MP PARA UNA ORDEN EXISTENTE
  @PostMapping("/{id}/payment")
  public ResponseEntity<?> createMpPayment(@PathVariable Long id) {
      try {
          Order order = orderRepo.findById(id)
                  .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));

          if (order.getItems() == null || order.getItems().isEmpty()) {
              return ResponseEntity.badRequest().body("La orden no tiene ítems");
          }

          // por las dudas
          order.recalculateTotals();
          log.info("Creando preferencia MP para orden {} total={}", order.getId(), order.getTotalAmount());

          Preference pref = mercadoPagoPreferenceService.createPreferenceForOrder(order);

          MpPreferenceResponse resp = new MpPreferenceResponse(
                  order.getId(),
                  pref.getId(),
                  pref.getInitPoint(),
                  order.getTotalAmount()
          );

          return ResponseEntity.ok(resp);

      } catch (MPApiException e) {
          // error de la API de Mercado Pago (token inválido, item inválido, etc.)
          log.error("Error API MercadoPago: status={} content={}",
                  e.getStatusCode(),
                  e.getApiResponse() != null ? e.getApiResponse().getContent() : "null",
                  e);

          String body = "MP_API_ERROR: " + (e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage());
          return ResponseEntity.status(502).body(body);

      } catch (MPException e) {
          log.error("Error SDK MercadoPago", e);
          return ResponseEntity.status(500).body("MP_SDK_ERROR: " + e.getMessage());

      } catch (Exception e) {
          log.error("Error inesperado al crear preferencia de pago", e);
          return ResponseEntity.status(500).body("SERVER_ERROR: " + e.getMessage());
      }
  }
  

  // LISTAR (q = razón social o CUIT, y status)
  @GetMapping
  public Page<OrderSummaryDTO> list(
      @RequestParam(name = "q", required = false) String razonOCuit,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    var result = listService.list(razonOCuit, status, page, size);
    audit.success(AuditAction.ORDER_LIST,
        "Listado de órdenes" + meta("q", razonOCuit) + meta("status", status));
    return result;
  }

  // ✅ Traer detalle por ID como DTO estable
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
    return orderRepo.findById(id)
        .map(o -> {
          audit.success(AuditAction.ORDER_READ, "Order", o.getId(), "Detalle");
          return ResponseEntity.ok(toResponse(o));
        })
        .orElseGet(() -> {
          audit.failure(AuditAction.ORDER_READ, "Order", id, "No encontrada");
          return ResponseEntity.notFound().build();
        });
  }

  @GetMapping("/statuses")
  public OrderStatus[] statuses() {
    return OrderStatus.values();
  }
  
  

  @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
    return orderRepo.findById(id)
        .map(o -> {
          // enriquecer con nombre y CUIT como hacés en el dialog
          final String[] name = {"—"};
          final String[] tax  = {"—"};
          if (o.getClientId()!=null) {
            clientRepo.findById(o.getClientId()).ifPresent(c -> {
              name[0] = safe(c.getName());
              tax[0]  = safe(c.getTaxId());
            });
          }

          byte[] pdf = orderPdf.build(o, name[0], tax[0]);

          // opcional: bitácora
          if (audit != null) {
            audit.success(AuditAction.GENERIC, "Order", o.getId(), "PDF de orden generado");
          }

          String filename = "orden-" + o.getId() + ".pdf";
          return ResponseEntity.ok()
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
              .contentType(MediaType.APPLICATION_PDF)
              .contentLength(pdf.length)
              .body(pdf);
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

//CREAR
@PostMapping
public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest body) {
   if (body == null || body.clientId() == null || body.items() == null || body.items().isEmpty()) {
       audit.failure(AuditAction.ORDER_CREATE, "Body inválido o incompleto");
       return ResponseEntity.badRequest().build();
   }

   if (clientRepo.findById(body.clientId()).isEmpty()) {
       audit.failure(AuditAction.ORDER_CREATE, "Client", body.clientId(), "Cliente inexistente");
       return ResponseEntity.badRequest().build();
   }

   try {
       Order order = new Order();
       order.setClientId(body.clientId());
       order.setStatus(OrderStatus.PENDING);
       order.setCreatedAt(Instant.now());
       order.setItems(new ArrayList<>());

       for (Item it : body.items()) {
           if (it == null || it.productId() == null || it.quantity() == null || it.quantity() <= 0) {
               audit.failure(AuditAction.ORDER_CREATE, "Item inválido");
               return ResponseEntity.badRequest().build();
           }

           // 🔹 Traemos el producto real
           var product = productRepo.findById(it.productId())
                   .orElseThrow(() -> new IllegalArgumentException("Producto inexistente: " + it.productId()));

           // 🔹 Construimos el item usando el factory (setea productName, unitPrice y lineTotal)
           OrderItem oi = OrderItem.of(
                   product.getId(),
                   product.getName(),
                   product.getUnitPrice(),
                   it.quantity()
           );

           // Relación bidireccional
           oi.setOrder(order);
           order.getItems().add(oi);
       }

       // Recalcular total de la orden (también se hace en @PrePersist, pero así queda explícito)
       order.recalculateTotals();

       Order saved = orderRepo.save(order);

       audit.success(
               AuditAction.ORDER_CREATE,
               "Order",
               saved.getId(),
               "Creada" + meta("items", saved.getItems() != null ? saved.getItems().size() : 0)
       );

       return ResponseEntity.ok(toResponse(saved));

   } catch (RuntimeException ex) {
       audit.failure(AuditAction.ORDER_CREATE, "Error al crear: " + safe(ex.getMessage()));
       throw ex;
   }
}


  // APROBAR (ADMIN o ADMIN_VENTAS)
  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS')")
  public ResponseEntity<Void> approve(@PathVariable Long id) {
    var opt = orderRepo.findById(id);
    if (opt.isEmpty()) {
      audit.failure(AuditAction.ORDER_APPROVE, "Order", id, "No encontrada");
      return ResponseEntity.notFound().build();
    }
    var o = opt.get();
    try {
      o.approve();
      orderRepo.save(o);
      audit.success(AuditAction.ORDER_APPROVE, "Order", id, "Aprobada");
      return ResponseEntity.noContent().build();
    } catch (RuntimeException ex) {
      audit.failure(AuditAction.ORDER_APPROVE, "Order", id, "Error al aprobar: " + safe(ex.getMessage()));
      throw ex;
    }
  }

  // ==== Requests mínimos ====
  public record CreateOrderRequest(Long clientId, List<Item> items) {}
  public record Item(Long productId, Integer quantity) {}

  // ==== Mapper entidad -> DTO ====
  private OrderResponse toResponse(Order o) {
    OrderResponse dto = new OrderResponse();
    dto.setId(o.getId());
    dto.setClientId(o.getClientId());
    dto.setStatus(o.getStatus());
    dto.setTotalAmount(o.getTotalAmount());
    dto.setCreatedAt(o.getCreatedAt());
    dto.setApprovedAt(o.getApprovedAt());

    if (o.getClientId() != null) {
      clientRepo.findById(o.getClientId()).ifPresent(c -> {
        dto.setClientName(safe(c.getName()));
        dto.setClientTaxId(safe(c.getTaxId()));
      });
    }

    List<OrderItemDTO> items = (o.getItems() == null) ? List.of()
        : o.getItems().stream().map(oi -> {
          OrderItemDTO x = new OrderItemDTO();
          x.setId(oi.getId());
          x.setProductId(oi.getProductId());
          x.setProductName(oi.getProductName());
          x.setUnitPrice(oi.getUnitPrice());
          x.setQuantity(oi.getQuantity());
          x.setLineTotal(oi.getLineTotal());
          return x;
        }).toList();
    dto.setItems(items);
    return dto;
  }

  private String safe(String s) { return (s == null || s.isBlank()) ? "—" : s; }

  /** Metadato simple clave=valor para messages del audit. */
  private String meta(String key, Object value) {
    return value == null ? "" : " [" + key + "=" + value + "]";
  }
}
