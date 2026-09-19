package com.portalfacturacion.adapter.in.security.jwt;

import com.portalfacturacion.application.port.out.security.TokenProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProviderPort tokenProvider;

  public JwtAuthenticationFilter(TokenProviderPort tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        if (tokenProvider.valido(token)) {
          String username = tokenProvider.obtenerUsuario(token);
          String tipo = tokenProvider.obtenerTipo(token);
          String role = "ROLE_" + (tipo == null ? "OPERADOR" : tipo);
          var auth = new UsernamePasswordAuthenticationToken(username, null,
              List.of(new SimpleGrantedAuthority(role)));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception ex) {
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String path = request.getServletPath();
    return "/error".equals(path)
        || path.startsWith("/api/v1/auth/")
        || "/api/v1/facturacion/datos-factura".equals(path)
        || "/api/v1/facturacion/factura".equals(path)
        || "/api/v1/facturacion/facturar".equals(path)
        || path.startsWith("/api/v1/images")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/actuator/health")
        || path.startsWith("/actuator/info");
  }
}
