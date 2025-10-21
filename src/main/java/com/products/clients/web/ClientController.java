package com.products.clients.web;

import com.products.clients.application.*;
import com.products.clients.domain.Address;
import com.products.clients.domain.Client;
import com.products.clients.web.dto.AddressDTO;
import com.products.clients.web.dto.ClientRequest;
import com.products.clients.web.dto.ClientResponse;

import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

  private final CreateClientUseCase createUC;
  private final UpdateClientUseCase updateUC;
  private final DeleteClientUseCase deleteUC;
  private final GetClientUseCase getUC;
  private final ListClientsUseCase listUC;
  private final AuditService audit;

  public ClientController(CreateClientUseCase createUC,
                          UpdateClientUseCase updateUC,
                          DeleteClientUseCase deleteUC,
                          GetClientUseCase getUC,
                          ListClientsUseCase listUC,
                          AuditService audit) {
    this.createUC = createUC;
    this.updateUC = updateUC;
    this.deleteUC = deleteUC;
    this.getUC = getUC;
    this.listUC = listUC;
    this.audit = audit;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS')")
  public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest req) {
    Client c = createUC.handle(
        req.getName(), req.getEmail(), req.getPhone(), req.getTaxId(),
        toAddresses(req.getAddresses())
    );
    audit.success(AuditAction.CLIENT_CREATE, "Client", c.getId(), "Creado: " + c.getName());
    return ResponseEntity.ok(toResponse(c));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS')")
  public ResponseEntity<ClientResponse> update(@PathVariable Long id, @Valid @RequestBody ClientRequest req) {
    Client c = updateUC.handle(
        id, req.getName(), req.getEmail(), req.getPhone(), req.getTaxId(),
        toAddresses(req.getAddresses())
    );
    audit.success(AuditAction.CLIENT_UPDATE, "Client", id, "Actualizado: " + c.getName());
    return ResponseEntity.ok(toResponse(c));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteUC.handle(id);
    audit.success(AuditAction.CLIENT_DELETE, "Client", id, "Eliminado");
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS','OPERADOR_VENTAS')")
  public ResponseEntity<ClientResponse> get(@PathVariable Long id) {
    return getUC.handle(id)
        .map(c -> {
          audit.success(AuditAction.CLIENT_READ, "Client", id, "Detalle");
          return ResponseEntity.ok(toResponse(c));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS','OPERADOR_VENTAS')")
  public Page<ClientResponse> list(@RequestParam(required = false) String name,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var result = listUC.handle(name, pageable).map(this::toResponse);
    audit.success(AuditAction.CLIENT_LIST, "Listado clientes");
    return result;
  }

  private List<Address> toAddresses(List<AddressDTO> dtos) {
    if (dtos == null) return List.of();
    return dtos.stream().map(dto -> {
      Address a = new Address();
      a.setId(dto.getId());
      a.setStreet(dto.getStreet());
      a.setCity(dto.getCity());
      a.setState(dto.getState());
      a.setZip(dto.getZip());
      a.setNotes(dto.getNotes());
      return a;
    }).toList();
  }

  private ClientResponse toResponse(Client c) {
    ClientResponse r = new ClientResponse();
    r.setId(c.getId());
    r.setName(c.getName());
    r.setEmail(c.getEmail());
    r.setPhone(c.getPhone());
    r.setTaxId(c.getTaxId());
    r.setCreatedAt(c.getCreatedAt());
    r.setUpdatedAt(c.getUpdatedAt());
    var addresses = c.getAddresses().stream().map(a -> {
      AddressDTO dto = new AddressDTO();
      dto.setId(a.getId());
      dto.setStreet(a.getStreet());
      dto.setCity(a.getCity());
      dto.setState(a.getState());
      dto.setZip(a.getZip());
      dto.setNotes(a.getNotes());
      return dto;
    }).toList();
    r.setAddresses(addresses);
    return r;
  }
}
