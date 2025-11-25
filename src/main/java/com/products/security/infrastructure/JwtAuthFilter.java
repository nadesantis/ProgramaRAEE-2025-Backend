package com.products.security.infrastructure;


import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
	@Autowired
  private JwtService jwtService;
	@Autowired
  private UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) { chain.doFilter(request, response); return; }

    var token = auth.substring(7);
    var username = jwtService.extractUsername(token);
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      var user = userDetailsService.loadUserByUsername(username);
      if (jwtService.isValid(token, user)) {
        var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    chain.doFilter(request, response);
  }
}
