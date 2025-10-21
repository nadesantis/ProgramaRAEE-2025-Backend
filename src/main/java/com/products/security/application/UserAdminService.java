package com.products.security.application;

import com.products.security.domain.Role;
import com.products.security.domain.User;
import com.products.security.infrastructure.repository.UserRepository;
import com.products.security.web.dto.CreateUserRequest;
import com.products.security.web.dto.UpdateUserRequest;
import com.products.security.web.dto.ResetPasswordRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAdminService {

  private final UserRepository repo;
  private final PasswordEncoder encoder;

  public UserAdminService(UserRepository repo, PasswordEncoder encoder) {
    this.repo = repo;
    this.encoder = encoder;
  }

  public Page<User> list(Pageable pageable) {
    return repo.findAll(pageable);
  }

  public User get(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
  }

  public User create(CreateUserRequest req) {
    final String email = normalizeEmail(req.email());
    repo.findByEmail(email).ifPresent(u -> { throw new IllegalArgumentException("Email ya registrado"); });

    if (req.password() == null || req.password().isBlank()) {
      throw new IllegalArgumentException("La contraseña es obligatoria");
    }

    User u = User.builder()
        .name(Objects.requireNonNullElse(req.name(), "").trim())
        .email(email)
        .password(encoder.encode(req.password()))
        .roles(normalizeRoles(req.roles())) 
        .build();

    return repo.save(u);
  }

  public User update(Long id, UpdateUserRequest req, Long actingUserId) {
    User u = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    if (req.name() != null) {
      u.setName(req.name().trim());
    }

    if (req.email() != null && !req.email().isBlank()) {
      String newEmail = normalizeEmail(req.email());
      if (!newEmail.equalsIgnoreCase(u.getEmail()) && repo.findByEmail(newEmail).isPresent()) {
        throw new IllegalArgumentException("Email ya registrado");
      }
      u.setEmail(newEmail);
    }

    if (req.roles() != null && !req.roles().isEmpty()) {
      boolean removingAdminRole =
          u.getRoles().contains(Role.ADMIN) && !req.roles().contains(Role.ADMIN);

      if (removingAdminRole && countAdminsExcept(u.getId()) == 0) {
        throw new IllegalStateException("Debe existir al menos un usuario ADMIN");
      }

      if (actingUserId != null && id.equals(actingUserId)
          && removingAdminRole && countAdminsExcept(u.getId()) == 0) {
        throw new IllegalStateException("No puedes quitarte el rol ADMIN si eres el único ADMIN");
      }

      u.setRoles(normalizeRoles(req.roles()));
    }

    return repo.save(u);
  }

  public void resetPassword(Long id, ResetPasswordRequest req) {
    if (req.newPassword() == null || req.newPassword().isBlank()) {
      throw new IllegalArgumentException("La nueva contraseña es obligatoria");
    }
    User u = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    u.setPassword(encoder.encode(req.newPassword()));
    u.resetFailedAttempts();
    repo.save(u);
  }

  public void unlock(Long id) {
    User u = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    u.resetFailedAttempts(); 
    repo.save(u);
  }

  public void delete(Long id, Long actingUserId) {
    if (actingUserId != null && id.equals(actingUserId)) {
      throw new IllegalStateException("No puedes eliminarte a ti mismo");
    }

    User u = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    if (u.getRoles().contains(Role.ADMIN) && countAdminsExcept(u.getId()) == 0) {
      throw new IllegalStateException("Debe existir al menos un usuario ADMIN");
    }

    repo.deleteById(id);
  }

  private String normalizeEmail(String email) {
    if (email == null) throw new IllegalArgumentException("Email obligatorio");
    return email.toLowerCase().trim();
  }

  private int countAdminsExcept(Long userId) {
    return (int) repo.findAll().stream()
        .filter(x -> !x.getId().equals(userId))
        .filter(x -> x.getRoles() != null && x.getRoles().contains(Role.ADMIN))
        .count();
  }

  private Set<Role> normalizeRoles(Set<Role> roles) {
    if (roles == null || roles.isEmpty()) {
      return Set.of(Role.USER); 
    }
    return roles.stream()
        .map(r -> {
          return r;
        })
        .collect(Collectors.toSet());
  }
}
