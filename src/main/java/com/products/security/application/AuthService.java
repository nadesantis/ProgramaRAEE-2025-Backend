package com.products.security.application;

import com.products.security.web.dto.AuthResponse;
import com.products.security.web.dto.GoogleLoginRequest;
import com.products.security.web.dto.LoginRequest;
import com.products.security.web.dto.RegisterRequest;
import com.products.security.domain.Role;
import com.products.security.domain.User;
import com.products.security.infrastructure.JwtService;
import com.products.security.infrastructure.repository.UserRepository;

import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;


import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
	@Autowired
  private AuthenticationManager authenticationManager;
	@Autowired
  private UserRepository userRepository;
	@Autowired
  private PasswordEncoder passwordEncoder;
	@Autowired
  private JwtService jwt;
	@Autowired
  private AuditService audit;
	@Autowired
  private GoogleIdTokenVerifier googleVerifier;
  private static final int MAX_ATTEMPTS = 3;

  @Transactional
  public AuthResponse register(RegisterRequest req) {
    final String email = req.email().toLowerCase().trim();

    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new IllegalArgumentException("Email ya registrado");
    }

    Role assignedRole;
    try {
      assignedRole = (req.role() == null || req.role().isBlank())
          ? Role.CLIENTE
          : Role.valueOf(req.role().toUpperCase());
    } catch (IllegalArgumentException ex) {
      assignedRole = Role.CLIENTE;
    }

    Set<Role> roles = Set.of(
        assignedRole == Role.CLIENTE ? Role.CLIENTE : Role.USER
    );

    User u = User.builder()
        .name(req.name())
        .email(email)
        .password(passwordEncoder.encode(req.password()))
        .roles(roles)
        .build();

    User saved = userRepository.save(u);

    UserDetails principal = org.springframework.security.core.userdetails.User
        .withUsername(saved.getEmail())
        .password(saved.getPassword())
        .authorities(saved.getRoles().stream().map(role -> "ROLE_" + role.name()).toArray(String[]::new))
        .build();

    String token = jwt.generate(principal, Map.of("roles", saved.getRoles()));
    audit.success(AuditAction.USER_CREATE, "User", saved.getId(), "Registro de usuario");

    Role firstRole = saved.getRoles().iterator().next();
    return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail(), firstRole.name());
  }

  @Transactional(noRollbackFor = {
      BadCredentialsException.class,
      AuthenticationException.class,
      LockedException.class
  })
  public AuthResponse login(LoginRequest req) {
    final String email = req.email().toLowerCase().trim();

    User u = userRepository.findByEmail(email)
        .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

    if (Boolean.TRUE.equals(u.getLocked())) {
      audit.failure(AuditAction.LOGIN_FAILURE, "Usuario bloqueado");
      throw new LockedException("Usuario bloqueado");
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, req.password())
      );

      u.resetFailedAttempts();
      userRepository.saveAndFlush(u);

      UserDetails principal = org.springframework.security.core.userdetails.User
          .withUsername(u.getEmail())
          .password(u.getPassword())
          .authorities(u.getRoles().stream().map(r -> "ROLE_" + r.name()).toArray(String[]::new))
          .build();

      String token = jwt.generate(principal, Map.of("roles", u.getRoles()));
      audit.success(AuditAction.LOGIN_SUCCESS, "Login exitoso");

      String firstRole = u.getRoles().stream().findFirst().map(Enum::name).orElse(Role.USER.name());
      return new AuthResponse(token, u.getId(), u.getName(), u.getEmail(), firstRole);

    } catch (BadCredentialsException e) {
      u.recordFailedAttempt(MAX_ATTEMPTS);
      userRepository.saveAndFlush(u);
      audit.failure(AuditAction.LOGIN_FAILURE, "Bad credentials");
      if (Boolean.TRUE.equals(u.getLocked())) throw new LockedException("Usuario bloqueado");
      throw e;

    } catch (AuthenticationException e) {
      u.recordFailedAttempt(MAX_ATTEMPTS);
      userRepository.saveAndFlush(u);
      audit.failure(AuditAction.LOGIN_FAILURE, "Authentication exception");
      if (Boolean.TRUE.equals(u.getLocked())) throw new LockedException("Usuario bloqueado");
      throw new BadCredentialsException("Credenciales inválidas");
    }
  }

  

  
  
  @Transactional
  public AuthResponse loginWithGoogle(GoogleLoginRequest req) {
      try {
          GoogleIdToken idToken = googleVerifier.verify(req.idToken());
          if (idToken == null) {
              audit.failure(AuditAction.LOGIN_FAILURE, "Google token inválido");
              throw new BadCredentialsException("Token de Google inválido");
          }

          Payload payload = idToken.getPayload();

          String email = ((String) payload.get("email")).toLowerCase().trim();

          String rawName = (String) payload.get("name");
          String finalName = (rawName == null || rawName.isBlank()) ? email : rawName;

          // Buscar usuario o crearlo como CLIENTE
          User u = userRepository.findByEmail(email)
              .orElseGet(() -> {
                  User nuevo = User.builder()
                      .name(finalName)
                      .email(email)
                      // cualquier password dummy
                      .password(passwordEncoder.encode("GOOGLE_LOGIN_" + payload.getSubject()))
                      .roles(Set.of(Role.CLIENTE))
                      .build();

                  return userRepository.save(nuevo);
              });


          if (Boolean.TRUE.equals(u.getLocked())) {
              audit.failure(AuditAction.LOGIN_FAILURE, "Usuario Google bloqueado");
              throw new LockedException("Usuario bloqueado");
          }

          UserDetails principal = org.springframework.security.core.userdetails.User
              .withUsername(u.getEmail())
              .password(u.getPassword())
              .authorities(u.getRoles().stream().map(r -> "ROLE_" + r.name()).toArray(String[]::new))
              .build();

          // ⚠️ ACÁ GENERÁS TU JWT, NO REUSÁS EL idToken DE GOOGLE
          String token = jwt.generate(principal, Map.of("roles", u.getRoles()));

          audit.success(AuditAction.LOGIN_SUCCESS, "Login Google exitoso");

          String firstRole = u.getRoles().stream()
              .findFirst().map(Enum::name).orElse(Role.USER.name());

          return new AuthResponse(token, u.getId(), u.getName(), u.getEmail(), firstRole);

      } catch (BadCredentialsException | LockedException ex) {
          throw ex;
      } catch (Exception e) {
          audit.failure(AuditAction.LOGIN_FAILURE, "Error login Google: " + e.getMessage());
          throw new BadCredentialsException("Error al validar Google");
      }
  }




}
