package org.example.ordermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.dto.request.SignInRequest;
import org.example.ordermanagement.dto.response.JwtAuthenticationResponse;
import org.example.ordermanagement.utils.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для авторизации пользователей")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Авторизация пользователя", description = "Позволяет пользователю войти в систему и получить JWT токен")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
            @ApiResponse(responseCode = "400", description = "Неверные данные для авторизации"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> signIn(@RequestBody @Valid SignInRequest request) {
        log.info("Вход пользователя с username: {}", request.getUsername());
        try {
            JwtAuthenticationResponse response = authenticationService.signIn(request);
            log.info("Успешная авторизация для пользователя: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка авторизации для пользователя: {}. Причина: {}", request.getUsername(), e.getMessage());
            throw e; // Пробрасываем исключение для дальнейшей обработки
        }
    }
}
