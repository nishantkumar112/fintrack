package com.fintrack.fintrack_dashboard.config;

import com.fintrack.fintrack_dashboard.security.JwtFilter;
import com.fintrack.fintrack_dashboard.security.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http

                /*
                 * CORS
                 */
                .cors(Customizer.withDefaults())

                /*
                 * Disable CSRF for stateless JWT APIs
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * Stateless session management
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Route authorization
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public endpoints
                         */
                        .requestMatchers(

                                "/auth/login",
                                "/auth/signup",
                                "/auth/refresh",
                                "/auth/forgot-password",
                                "/auth/reset-password",

                                "/oauth2/**",
                                "/login/**"

                        ).permitAll()

                        /*
                         * Protected endpoints
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * OAuth2 Login
                 */
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                )

                /*
                 * JWT Filter
                 */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}