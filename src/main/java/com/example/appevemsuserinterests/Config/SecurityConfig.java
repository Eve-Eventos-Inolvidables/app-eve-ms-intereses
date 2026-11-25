package com.example.appevemsuserinterests.Config;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${authentication.enabled:true}")
    private boolean enabled;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.algorithm}")
    private String algorithm;

    @Value("${jwt.issuer:}")
    private String issuer;

    @Value("${jwt.clock-skew-seconds:0}")
    private long clockSkewSeconds;

    @Value("${jwt.claim.roles:role}")
    private String rolesClaim;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // El API Gateway ya se encarga de CORS
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!enabled) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/user-interests/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(authenticationConverter())));
        return http.build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/app-eve-ms-userinterests").description("Ruta vía API Gateway"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    private JwtDecoder jwtDecoder() {
        MacAlgorithm mac = MacAlgorithm.valueOf(algorithm);
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(secret),
                "HmacSHA" + algorithm.substring(2));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(mac).build();

        JwtTimestampValidator timestamp = new JwtTimestampValidator(Duration.ofSeconds(clockSkewSeconds));
        OAuth2TokenValidator<Jwt> validator = issuer.isBlank()
                ? new DelegatingOAuth2TokenValidator<>(timestamp)
                : new DelegatingOAuth2TokenValidator<>(timestamp, new JwtIssuerValidator(issuer));
        decoder.setJwtValidator(validator);
        return decoder;
    }

    private JwtAuthenticationConverter authenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object claim = jwt.getClaim(rolesClaim);
            if (claim == null) claim = jwt.getClaim("role");
            if (claim == null) claim = jwt.getClaim("roles");

            if (claim instanceof List<?> list) {
                return list.stream()
                        .map(Object::toString)
                        .<GrantedAuthority>map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                        .toList();
            }

            if (claim instanceof String str && !str.isBlank()) {
                return List.<GrantedAuthority>of(
                        new SimpleGrantedAuthority("ROLE_" + str.toUpperCase())
                );
            }

            return List.<GrantedAuthority>of();
        });
        return converter;
    }
}