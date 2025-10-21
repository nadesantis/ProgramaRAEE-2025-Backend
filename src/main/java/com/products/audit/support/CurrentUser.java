package com.products.audit.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;

public class CurrentUser {

  public static String email() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null) return null;
    return a.getName(); 
  }

  public static String rolesCsv() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null) return null;
    return a.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority) // "ROLE_ADMIN"
        .collect(Collectors.joining(","));
  }

  public static String clientIp(HttpServletRequest req) {
    if (req == null) return null;
    String forwarded = req.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
    return req.getRemoteAddr();
  }
}
