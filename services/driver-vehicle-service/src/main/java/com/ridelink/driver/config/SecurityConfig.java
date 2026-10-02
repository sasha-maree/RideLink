package com.ridelink.driver.config;

import com.ridelink.driver.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // Drivers Profile
                .requestMatchers(HttpMethod.POST, "/api/v1/drivers").hasRole("DRIVER")
                .requestMatchers(HttpMethod.GET, "/api/v1/drivers").hasRole("ADMIN")
                .requestMatchers("/api/v1/drivers/me", "/api/v1/drivers/me/availability").hasRole("DRIVER")
                
                // Internal Endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/drivers/available").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/drivers/{driverId}").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/drivers/{driverId}/availability").authenticated()
                
                // Vehicles
                .requestMatchers(HttpMethod.POST, "/api/v1/vehicles").hasRole("DRIVER")
                .requestMatchers(HttpMethod.GET, "/api/v1/vehicles/me").hasRole("DRIVER")
                .requestMatchers(HttpMethod.GET, "/api/v1/vehicles/{vehicleId}").authenticated()

                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
