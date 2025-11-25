package com.products.security.web;


import com.products.security.web.dto.*;
import com.products.security.application.AuthService;
import com.products.security.web.dto.AuthResponse;
import com.products.security.web.dto.LoginRequest;
import com.products.security.web.dto.RegisterRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	@Autowired
private AuthService auth;

@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
 return ResponseEntity.ok(auth.register(req));
}

@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
 return ResponseEntity.ok(auth.login(req));
}

@PostMapping("/google-login")
public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest req) {
  return ResponseEntity.ok(auth.loginWithGoogle(req));
}
}

