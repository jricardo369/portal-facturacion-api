package com.portalfacturacion.configuration;

import com.portalfacturacion.adapter.in.security.jwt.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
                                         @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource") CorsConfigurationSource corsSource) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsSource))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/facturacion/datos-factura", "/api/v1/facturacion/factura", "/api/v1/images/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/facturacion/facturar").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/facturacion/enviar-correo", "/api/v1/facturacion/reenviar-factura").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**").hasAnyRole("ADMIN", "OPERADOR", "CONSULTA")
            .requestMatchers("/api/v1/tickets/**").authenticated()
            .requestMatchers("/api/v1/facturacion/**").hasAnyRole("ADMIN", "OPERADOR", "CONSULTA")
            .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").hasAnyRole("ADMIN", "OPERADOR", "CONSULTA")
            .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasAnyRole("ADMIN", "OPERADOR", "CONSULTA")
            .requestMatchers(HttpMethod.POST, "/api/v1/clientes/**").hasAnyRole("ADMIN", "OPERADOR")
            .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**").hasAnyRole("ADMIN", "OPERADOR")
            .requestMatchers("/api/v1/clientes/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(e -> e
            .authenticationEntryPoint((req, res, ex) -> {
              res.setStatus(401);
              res.setContentType("application/problem+json");
              res.getWriter().write("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Token no valido o ausente\"}");
            })
            .accessDeniedHandler((req, res, ex) -> {
              res.setStatus(403);
              res.setContentType("application/problem+json");
              res.getWriter().write("{\"title\":\"Forbidden\",\"status\":403,\"detail\":\"No tiene permisos suficientes\"}");
            }));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200}") String origins) {
    CorsConfiguration config = new CorsConfiguration();
    List<String> list = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    config.setAllowedOriginPatterns(list);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
