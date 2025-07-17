package kg.manurov.tasktracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.manurov.tasktracker.domain.dto.RegistrationDto;
import kg.manurov.tasktracker.domain.models.User;
import kg.manurov.tasktracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для регистрации нового пользователя")
public class AuthController {

    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegistrationDto registrationDto) {
        log.info("Регистрация нового пользователя: {}", registrationDto.getEmail());
        if (userService.existsByEmail(registrationDto.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Пользователь с таким email уже существует"));
        }
        User createdUser = userService.create(registrationDto);
        log.info("Пользователь успешно зарегистрирован: {}", createdUser.getEmail());

        return ResponseEntity.ok(HttpStatus.CREATED);
    }
}