package com.brasfi.webapp.controller.api;

import com.brasfi.webapp.dto.ApiDtos.AuthResponse;
import com.brasfi.webapp.dto.ApiDtos.ErrorResponse;
import com.brasfi.webapp.dto.ApiDtos.RegisterRequest;
import com.brasfi.webapp.dto.ApiDtos.UserResponse;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.security.CustomUserDetails;
import com.brasfi.webapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ApiAuthController {
    private final UserService userService;

    public ApiAuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return new AuthResponse(false, null);
        }

        return new AuthResponse(true, UserResponse.from(currentUser.getUserEntity()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                    request.nome(),
                    request.email(),
                    request.cpf(),
                    request.senha(),
                    request.idade() == null ? 0 : request.idade()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
        }
    }
}
