package com.brasfi.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/register", "/sobre", "/css/**", "/js/**", "/img/**", "/error").permitAll()
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/me").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/dashboard", "/api/v1/events/**", "/api/v1/communities/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    if (wantsJson(request.getHeader("X-Requested-With"), request.getHeader("Accept"))) {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"authenticated\":true}");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    if (wantsJson(request.getHeader("X-Requested-With"), request.getHeader("Accept"))) {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"message\":\"Email ou senha invalidos.\"}");
                    } else {
                        response.sendRedirect("/login?error");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .logoutSuccessHandler((request, response, authentication) -> {
                    if (wantsJson(request.getHeader("X-Requested-With"), request.getHeader("Accept"))) {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"authenticated\":false}");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            );

        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String origins = System.getenv().getOrDefault(
                "APP_CORS_ALLOWED_ORIGINS",
                "http://localhost:3000,http://localhost:5173,http://localhost:8080"
        );
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static boolean wantsJson(String requestedWith, String accept) throws IOException {
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }
}
