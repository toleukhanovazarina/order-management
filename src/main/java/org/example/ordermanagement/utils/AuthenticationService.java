package org.example.ordermanagement.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.db.entity.User;
import org.example.ordermanagement.db.repository.UserRepository;
import org.example.ordermanagement.dto.request.SignInRequest;
import org.example.ordermanagement.dto.response.JwtAuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger actionLogger = LoggerFactory.getLogger("org.example.ordermanagement.actions");

    /**
     * Аутентификация
     *
     * @param request данные
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        String username = request.getUsername();
        log.info("Authentication attempt for username: {}", username);
        actionLogger.info("Action: AuthenticationAttempt - Username: {}", username);

        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                log.warn("User not found for username: {}", username);
                actionLogger.warn("Action: AuthenticationFailed - Reason: UserNotFound - Username: {}", username);
                throw new BadCredentialsException("Invalid username or password");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Invalid password for username: {}", username);
                actionLogger.warn("Action: AuthenticationFailed - Reason: InvalidPassword - Username: {}", username);
                throw new BadCredentialsException("Invalid username or password");
            }

            String jwt = jwtService.generateToken(user);
            log.info("Authentication successful for username: {}", username);
            actionLogger.info("Action: AuthenticationSuccess - Username: {}", username);

            return new JwtAuthenticationResponse(jwt, "Success");

        } catch (BadCredentialsException ex) {
            log.error("Authentication failed for username: {}. Reason: {}", username, ex.getMessage());
            actionLogger.error("Action: AuthenticationFailed - Reason: {} - Username: {}", ex.getMessage(), username);
            return new JwtAuthenticationResponse(null, "Invalid username or password");
        } catch (Exception ex) {
            log.error("Unexpected error during authentication for username: {}. Reason: {}", username, ex.getMessage(), ex);
            actionLogger.error("Action: AuthenticationFailed - Reason: UnexpectedError - Username: {}", username);
            return new JwtAuthenticationResponse(null, "Authentication failed due to an unexpected error");
        }
    }
}

