package com.products.security.web;

import com.products.security.domain.User;
import com.products.security.infrastructure.repository.UserRepository;
import com.products.security.web.dto.SimpleUserDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

	@Autowired
  private UserRepository users;

  public UsersController(UserRepository users) { this.users = users; }

  @GetMapping("/technicians")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO')")
  public List<SimpleUserDTO> listTechnicians() {
    return users.findTechnicians()
        .stream()
        .map(u -> new SimpleUserDTO(u.getId(), u.getName(), u.getEmail()))
        .toList();
  }
}