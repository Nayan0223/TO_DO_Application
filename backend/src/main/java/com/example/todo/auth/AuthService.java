package com.example.todo.auth;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository users,
            AuthSessionRepository sessions,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.users = users;
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.Credentials credentials) {
        String email = normalizeEmail(credentials.email());
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        UserAccount user = users.save(new UserAccount(
                java.util.UUID.randomUUID().toString(),
                email,
                passwordEncoder.encode(credentials.password())
        ));
        return createSession(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.Credentials credentials) {
        String email = normalizeEmail(credentials.email());
        UserAccount user = users.findByEmail(email)
                .filter(account -> passwordEncoder.matches(credentials.password(), account.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
        return createSession(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse currentUser(String email) {
        return users.findByEmail(email)
                .map(user -> new AuthDtos.UserResponse(user.getEmail()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    @Transactional
    public void logout(String token) {
        Claims claims = jwtService.parse(token);
        AuthSession session = sessions.findById(claims.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session not found"));
        if (session.getRevokedAt() == null) {
            session.revoke(Instant.now());
            sessions.save(session);
        }
    }

    private AuthDtos.AuthResponse createSession(UserAccount user) {
        String tokenId = jwtService.newTokenId();
        Instant expiresAt = jwtService.newExpiration();
        sessions.save(new AuthSession(tokenId, user, expiresAt));
        return new AuthDtos.AuthResponse(jwtService.createToken(user, tokenId, expiresAt), user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
