package com.products.security.web.dto;

import com.products.security.domain.Role;
import java.util.Set;

public record CreateUserRequest(
    String name,
    String email,
    String password,
    Set<Role> roles
) {}
