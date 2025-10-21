package com.products.security.web.dto;

import com.products.security.domain.Role;
import com.products.security.domain.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
    Long id,
    String name,
    String email,
    Set<String> roles
) {
    public static UserResponse fromEntity(User u) {
        Set<String> roles = u.getRoles() == null
                ? Set.of()
                : u.getRoles().stream().map(Role::name).collect(Collectors.toSet());
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), roles);
    }
}
