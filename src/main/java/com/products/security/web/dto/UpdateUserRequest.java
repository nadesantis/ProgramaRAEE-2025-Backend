package com.products.security.web.dto;

import com.products.security.domain.Role;
import java.util.Set;

public record UpdateUserRequest(
    String name,      
    String email,    
    Set<Role> roles    
) {}
