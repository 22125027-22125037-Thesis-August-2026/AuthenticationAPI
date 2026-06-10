package com.mhsa.backend.tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mhsa.backend.auth.jwt.JwtAuthenticationFilter;
import com.mhsa.backend.auth.jwt.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/internal/v1/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/v1/tracking/**").authenticated()
                        .anyRequest().authenticated()
                )
                // Return 401 for authentication failures (missing/expired/invalid token)
                // and reserve 403 for genuine authorization denials. Without this, Spring
                // Security's default Http403ForbiddenEntryPoint returns 403 for both.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e)
                                -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, e)
                                -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
