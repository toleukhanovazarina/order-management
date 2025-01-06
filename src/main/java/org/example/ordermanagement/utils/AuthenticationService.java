package org.example.ordermanagement.utils;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.db.entity.Admin;
import org.example.ordermanagement.db.entity.Customer;
import org.example.ordermanagement.db.repository.AdminRepository;
import org.example.ordermanagement.db.repository.CustomerRepository;
import org.example.ordermanagement.dto.request.SignInRequest;
import org.example.ordermanagement.dto.response.JwtAuthenticationResponse;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
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
            Customer customer = customerRepository.findByUsername(username);
            if (customer != null && passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
                var jwt = jwtService.generateToken(customer);
                return new JwtAuthenticationResponse(jwt, "Success");
            }

            Admin admin = adminRepository.findByUsername(username);
            if (admin != null && passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                var jwt = jwtService.generateTokenAdmin(admin);
                return new JwtAuthenticationResponse(jwt, "Success");
            }

            throw new BadCredentialsException("Invalid username or password");
        } catch (BadCredentialsException ex) {
            return new JwtAuthenticationResponse(null, "Invalid username or password");
        }
    }

}

