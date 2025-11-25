package com.products.security.infrastructure;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

private final SecretKey key;
private final long expiration;

public JwtService(
   @Value("${jwt.secret}") String secret,
   @Value("${jwt.expiration}") long expirationSeconds) {
 this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
 this.expiration = expirationSeconds;
}

public String generate(UserDetails user, Map<String, Object> claims) {
 Instant now = Instant.now();
 return Jwts.builder()
     .setSubject(user.getUsername())
     .addClaims(claims)
     .setIssuedAt(Date.from(now))
     .setExpiration(Date.from(now.plusSeconds(expiration)))
     .signWith(key, SignatureAlgorithm.HS256)
     .compact();
}

public String extractUsername(String token) {
 return parse(token).getBody().getSubject();
}

public boolean isValid(String token, UserDetails user) {
 Claims claims = parse(token).getBody();
 return user.getUsername().equals(claims.getSubject()) &&
        claims.getExpiration().after(new Date());
}

private Jws<Claims> parse(String token) {
 return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
}
}
