package com.example.eventura.config;

import com.example.eventura.security.JwtAuthenticationFilter;
import com.example.eventura.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtRequestFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtRequestFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").authenticated() // Explicitly require authentication for all /api/users endpoints
                        .requestMatchers("/api/providers").permitAll() // Allow public access to list providers
                        .requestMatchers("/api/providers/{providerId}").hasAnyRole("CLIENT", "PROVIDER", "ADMIN") // Allow CLIENT, PROVIDER, ADMIN to get provider profile by ID
                        .requestMatchers("/api/providers/**").hasAnyRole("PROVIDER", "ADMIN","CLIENT") // Restrict other provider endpoints to PROVIDER, ADMIN
                        .requestMatchers("/api/requests/**").hasAnyRole("CLIENT", "ADMIN", "PROVIDER")
                        .requestMatchers("/api/pitches/**").hasAnyRole("PROVIDER", "CLIENT", "ADMIN")
                        .requestMatchers("/api/payments/**").hasAnyRole("CLIENT", "PROVIDER", "ADMIN")
                        .requestMatchers("/api/reviews/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/notifications/**").hasAnyRole("CLIENT", "PROVIDER", "ADMIN")
                        .requestMatchers("/api/portfolios/**").hasAnyRole("PROVIDER", "ADMIN","CLIENT")
                        .requestMatchers("/api/verifications/**").hasAnyRole("PROVIDER", "ADMIN")
                        .requestMatchers("/api/users/profile", "/api/users/{userId}").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}