package com.products.security.web.dto;

public record AuthResponse(
    String token,
    Long id,
    String name,
    String email,
    String role
) {}

