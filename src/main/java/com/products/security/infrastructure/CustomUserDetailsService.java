package com.products.security.infrastructure;

import com.products.security.domain.User;
import com.products.security.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final String email = (username == null) ? null : username.toLowerCase().trim();

    User u = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    boolean locked = Boolean.TRUE.equals(u.getLocked());

    Set<?> roles = (u.getRoles() == null) ? Set.of() : u.getRoles();

    return org.springframework.security.core.userdetails.User
        .withUsername(u.getEmail())
        .password(u.getPassword())
        .authorities(
            roles.stream()
                 .map(r -> "ROLE_" + r.toString())  
                 .toArray(String[]::new)
        )
        .accountLocked(locked)
        .accountExpired(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }
}
