package com.products.security.web;

import com.products.security.application.UserAdminService;
import com.products.security.domain.Role;
import com.products.security.domain.User;
import com.products.security.web.dto.CreateUserRequest;
import com.products.security.web.dto.ResetPasswordRequest;
import com.products.security.web.dto.UpdateUserRequest;
import com.products.security.web.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService service;

    public UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UserResponse> list(Pageable pageable) {
        return service.list(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        User u = service.get(id);
        return toResponse(u);
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        User u = service.create(req);
        return toResponse(u);
    }
    
    @PostMapping("/{id}/unlock")
    public void unlock(@PathVariable Long id) {
      service.unlock(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateUserRequest req,
                               Authentication auth) {
        Long actingUserId = null;
        User u = service.update(id, req, actingUserId);
        return toResponse(u);
    }

    @PostMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable Long id,
                              @Valid @RequestBody ResetPasswordRequest req) {
        service.resetPassword(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Long actingUserId = null;
        service.delete(id, actingUserId);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRoles() == null
                        ? null
                        : u.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );
    }
}
