// src/main/java/com/trackingpath/configs/SecurityConfiguration.java
package com.trackingpath.configs;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwt;

    public SecurityConfiguration(AuthenticationProvider ap, JwtAuthenticationFilter jwt) {
        this.authenticationProvider = ap;
        this.jwt = jwt;
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/traccar/**",
                        "/error"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/parent/auth/login",
                        "/api/parent/auth/forgot-password",
                        "/api/parent/auth/reset-password")
                .permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/driver/auth/login",
                        "/api/driver/auth/forgot-password",
                        "/api/driver/auth/reset-password")
                .permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/snapshots/**").permitAll()
                .requestMatchers("/devicemodal/**").permitAll()
                .requestMatchers("/auth/**", "/hook/**", "/ws/**","/api/ws/**").permitAll()
                .requestMatchers("/ws-chat/**").permitAll()
                .requestMatchers("/chatUploads/**").permitAll()
                .requestMatchers("/soundUploads/**").permitAll()
                
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/api/parent/**").hasRole("PARENT")
                .requestMatchers("/api/driver/**").hasRole("DRIVER")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();

        cors.setAllowedOriginPatterns(
                List.of("http://localhost:3000", "http://127.0.0.1:3000", "https://fleetplus.trackingpath.com",
                        "http://fleetplus.trackingpath.com")
        );
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
