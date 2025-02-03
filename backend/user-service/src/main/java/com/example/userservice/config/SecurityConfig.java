package com.example.userservice.config;


import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("test")
    UserDetailsService inMemoryUserDetailsManager() {
        UserDetails testUser = User.builder()
            .username("testuser")
            .password(passwordEncoder().encode("testpass"))
            .roles("USER")
            .build();

        UserDetails adminUser = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("adminpass"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(testUser, adminUser);
    }

    @Bean
    @Profile("!test")
    UserDetailsService userDetailsService() {
        // 本番用(ダミー)
        return new InMemoryUserDetailsManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers("/api/users/**").authenticated()
                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(customAuthenticationEntryPoint())
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return converter;
    }



	//カスタムJSON設定
	//http 401 error
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            log.warn("Unauthorized access attempt: {}", request.getRequestURI());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("status", 401);
            body.put("error", "Unauthorized");
            body.put("message", "Full authentication is required to access this resource");
            body.put("path", request.getRequestURI());

            Map<String, List<String>> details = new LinkedHashMap<>();
            details.put("errors", List.of("requiredAuth: Bearer Token"));
            body.put("details", details);

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), body);
        };
    }

	//http 403 error
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("Access denied to resource: {}", request.getRequestURI());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("status", 403);
            body.put("error", "Forbidden");
            body.put("message", "Access Denied");
            body.put("path", request.getRequestURI());

            Map<String, List<String>> details = new LinkedHashMap<>();
            details.put("errors", List.of("アクセスが拒否されました"));
            body.put("details", details);

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), body);
        };
    }
}
