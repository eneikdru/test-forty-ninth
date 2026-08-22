package com.eneik.production.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.default-user.username}")
    private String defaultUsername;

    @Value("${app.security.default-user.password}")
    private String defaultPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/protocols/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/materials").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/materials/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/materials").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/materials/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/materials").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/materials/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
            .username(defaultUsername)
            .password(passwordEncoder.encode(defaultPassword))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
