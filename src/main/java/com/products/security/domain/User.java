package com.products.security.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity 
@Table(name = "users")
public class User {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)  
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)  
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    
    @Column(nullable = false)
    private int failedAttempts = 0;

    @Column(nullable = false)
    private boolean locked = false;
    
    private Instant lockedAt;

    public User() {}

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean getLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public Instant getLockedAt() { return lockedAt; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }

    // lógica de intentos/bloqueo
    public void recordFailedAttempt(int maxAttempts) {
        this.failedAttempts = Math.min(this.failedAttempts + 1, maxAttempts);
        if (this.failedAttempts >= maxAttempts) {
            this.locked = true;
            this.lockedAt = Instant.now();
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.locked = false;
        this.lockedAt = null;
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private String name; private String email; private String password; private Set<Role> roles = new HashSet<>();
        public Builder id(Long id){ this.id=id; return this; }
        public Builder name(String name){ this.name=name; return this; }
        public Builder email(String email){ this.email=email; return this; }
        public Builder password(String password){ this.password=password; return this; }
        public Builder roles(Set<Role> roles){ this.roles=roles; return this; }
        public User build(){ 
            User u = new User(); 
            u.id=id; u.name=name; u.email=email; u.password=password; u.roles=roles; 
            return u; 
        }
    }
}
