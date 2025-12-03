package com.acainfo.security.userdetails;

import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation.
 * Loads user from database using UserRepositoryPort (hexagonal architecture).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepositoryPort.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }

    /**
     * Load user by ID (used for token refresh).
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        return userRepositoryPort.findById(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
