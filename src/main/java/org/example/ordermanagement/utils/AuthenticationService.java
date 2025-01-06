package org.example.ordermanagement.utils;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.db.entity.User;
import org.example.ordermanagement.db.repository.UserRepository;
import org.example.ordermanagement.dto.request.SignInRequest;
import org.example.ordermanagement.dto.response.JwtAuthenticationResponse;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Аутентификация
     *
     * @param request данные
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        try {
            String username = request.getUsername();
            User user = userRepository.findByUsername(username);
            if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                var jwt = jwtService.generateToken(user);
                return new JwtAuthenticationResponse(jwt, "Success");
            }

            throw new BadCredentialsException("Invalid username or password");
        } catch (BadCredentialsException ex) {
            return new JwtAuthenticationResponse(null, "Invalid username or password");
        }
    }

}

