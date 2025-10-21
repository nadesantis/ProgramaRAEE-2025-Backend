package com.products.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {

    response.setContentType("application/json;charset=UTF-8");

    if (ex instanceof LockedException) {
      response.setStatus(423); 
      response.getWriter().write("{\"status\":423,\"error\":\"Usuario bloqueado\"}");
    } else {
      response.setStatus(401);
      response.getWriter().write("{\"status\":401,\"error\":\"Credenciales inválidas\"}");
    }
  }
}
