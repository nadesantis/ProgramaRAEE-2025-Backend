// src/main/java/com/products/orders/application/ListSalesOrdersService.java
package com.products.orders.application;

import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderRepository;
import com.products.orders.domain.OrderStatus;
import com.products.orders.web.dto.OrderSummaryDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListSalesOrdersService {
	@Autowired
  private  OrderRepository orderRepo;
	@Autowired
  private  ClientRepository clientRepo;

  @Transactional(readOnly = true)
  public Page<OrderSummaryDTO> list(String razonOCuit, OrderStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    // 1) Filtrar universo de clientes (razón/CUIT)
    Collection<Long> clientIdsFilter = null;
    if (razonOCuit != null && !razonOCuit.isBlank()) {
      String q = razonOCuit.trim();
      var clients = clientRepo.findByNameContainingIgnoreCaseOrTaxIdContainingIgnoreCase(q, q);
      if (clients.isEmpty()) return Page.empty(pageable);
      clientIdsFilter = clients.stream().map(Client::getId).collect(Collectors.toSet());
    }

    // 2) Traer órdenes SIN fetch de items
    Page<Order> orders;
    if (clientIdsFilter != null && status != null) {
      orders = orderRepo.findByClientIdInAndStatus(clientIdsFilter, status, pageable);
    } else if (clientIdsFilter != null) {
      orders = orderRepo.findByClientIdIn(clientIdsFilter, pageable);
    } else if (status != null) {
      orders = orderRepo.findByStatus(status, pageable);
    } else {
      orders = orderRepo.findAll(pageable);
    }

    // 3) Mapear a DTO (sin tocar o.getItems())
    Map<Long, Client> clientCache = new HashMap<>();

    return orders.map(o -> {
      Client c = null;
      if (o.getClientId() != null) {
        c = clientCache.computeIfAbsent(o.getClientId(), id -> clientRepo.findById(id).orElse(null));
      }

      // En LISTADO no accedemos a items; productNames vacío (el front mostrará "—")
      List<String> productNames = List.of();

      return new OrderSummaryDTO(
          o.getId(),
          o.getCreatedAt(),
          o.getStatus(),
          o.getTotalAmount(),
          c != null ? nullSafe(c.getName())  : "—",
          c != null ? nullSafe(c.getTaxId()) : "—",
          productNames
      );
    });
  }

  private String nullSafe(String s) {
    return (s == null || s.isBlank()) ? "—" : s;
  }
}
