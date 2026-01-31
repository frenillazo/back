package com.acainfo.security.userdetails;

import com.acainfo.user.domain.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation wrapping domain User entity.
 * Adapts domain User to Spring Security UserDetails interface.
 */
@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getType().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Email is the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't have account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        // Siempre true - la validación de estado se hace en AuthService.authenticate()
        // Esto permite que usuarios BLOCKED autentiquen y vean el banner de cuenta restringida
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // We don't have password expiration
    }

    @Override
    public boolean isEnabled() {
        // Siempre true - la validación de estado se hace en AuthService.authenticate()
        // Esto permite que usuarios INACTIVE autentiquen y vean el banner de cuenta restringida
        // Solo usuarios PENDING_ACTIVATION son rechazados (403) en la capa de servicio
        return true;
    }

    /**
     * Get user ID.
     */
    public Long getUserId() {
        return user.getId();
    }
}
