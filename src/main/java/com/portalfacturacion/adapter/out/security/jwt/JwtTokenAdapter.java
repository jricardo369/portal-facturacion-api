package com.portalfacturacion.adapter.out.security.jwt;

import com.portalfacturacion.application.port.out.security.TokenProviderPort;
import com.portalfacturacion.configuration.JwtProperties;
import com.portalfacturacion.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenAdapter implements TokenProviderPort {

  private final JwtProperties properties;
  private final SecretKey key;

  public JwtTokenAdapter(JwtProperties properties) {
    this.properties = properties;
    this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String generar(Usuario usuario) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + properties.getExpirationMs());
    return Jwts.builder()
        .issuer(properties.getIssuer())
        .audience().add(properties.getAudience()).and()
        .subject(usuario.getUsuario())
        .claim("uid", usuario.getId())
        .claim("tipo", usuario.getTipo() == null ? null : usuario.getTipo().name())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key)
        .compact();
  }

  @Override
  public boolean valido(String token) {
    try {
      parse(token);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  @Override
  public String obtenerUsuario(String token) {
    return parse(token).getSubject();
  }

  @Override
  public Long obtenerId(String token) {
    Object uid = parse(token).get("uid");
    if (uid instanceof Number n) {
      return n.longValue();
    }
    return uid == null ? null : Long.valueOf(uid.toString());
  }

  @Override
  public String obtenerTipo(String token) {
    Object tipo = parse(token).get("tipo");
    return tipo == null ? null : tipo.toString();
  }

  private Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .requireIssuer(properties.getIssuer())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
