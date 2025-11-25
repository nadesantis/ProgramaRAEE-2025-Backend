package com.products.security.infrastructure;

import jakarta.servlet.http.HttpServletResponse;            // <- Jakarta, no javax
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	@Autowired
  private JwtAuthFilter jwtAuthFilter;
	@Autowired
  private CustomUserDetailsService uds;

  @Bean
  public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
                                         AuthenticationProvider authenticationProvider) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
        .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((req, res, exAuth) -> writeAuthError(res, exAuth))
        .accessDeniedHandler((req, res, exAcc) -> {
          res.setStatus(HttpStatus.FORBIDDEN.value());
          res.setContentType(MediaType.APPLICATION_JSON_VALUE);
          res.getWriter().write("{\"status\":403,\"error\":\"Acceso denegado\"}");
        })
      )
      .authenticationProvider(authenticationProvider)
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:5173"));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    c.setAllowedHeaders(List.of("*"));
    c.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/**", c);
    return s;
  }

  private void writeAuthError(HttpServletResponse res, AuthenticationException exAuth)
      throws IOException {

    int status;
    String message;

    if (exAuth instanceof LockedException) {
      status = HttpStatus.LOCKED.value();       
      message = "Usuario bloqueado";
    } else if (exAuth instanceof BadCredentialsException) {
      status = HttpStatus.UNAUTHORIZED.value();  
      message = "Credenciales inválidas";
    } else if (exAuth instanceof DisabledException) {
      status = HttpStatus.FORBIDDEN.value();    
      message = "Usuario deshabilitado";
    } else {
      status = HttpStatus.UNAUTHORIZED.value();  
      message = "No autorizado";
    }

    res.setStatus(status);
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.getWriter().write("{\"status\":" + status + ",\"error\":\"" + message + "\"}");
  }
}
