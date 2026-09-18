package com.portalfacturacion.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.correo")
public class CorreoProperties {

  private String host;
  private int port = 587;
  private String username;
  private String password;
  private String remitente;
  private String nombreRemitente = "Portal Facturacion";
  private boolean auth = true;
  private boolean starttls = true;
  private long timeoutMs = 8000;
  private String rutaLayouts;
  private String rutaLogo;

  public String getHost() { return host; }
  public void setHost(String host) { this.host = host; }
  public int getPort() { return port; }
  public void setPort(int port) { this.port = port; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getRemitente() { return remitente; }
  public void setRemitente(String remitente) { this.remitente = remitente; }
  public String getNombreRemitente() { return nombreRemitente; }
  public void setNombreRemitente(String nombreRemitente) { this.nombreRemitente = nombreRemitente; }
  public boolean isAuth() { return auth; }
  public void setAuth(boolean auth) { this.auth = auth; }
  public boolean isStarttls() { return starttls; }
  public void setStarttls(boolean starttls) { this.starttls = starttls; }
  public long getTimeoutMs() { return timeoutMs; }
  public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
  public String getRutaLayouts() { return rutaLayouts; }
  public void setRutaLayouts(String rutaLayouts) { this.rutaLayouts = rutaLayouts; }
  public String getRutaLogo() { return rutaLogo; }
  public void setRutaLogo(String rutaLogo) { this.rutaLogo = rutaLogo; }

  public boolean isConfigured() {
    return host != null && !host.isBlank() && remitenteEfectivo() != null && !remitenteEfectivo().isBlank();
  }

  public String remitenteEfectivo() {
    if (username != null && !username.isBlank()) {
      return username;
    }
    return remitente;
  }
}
