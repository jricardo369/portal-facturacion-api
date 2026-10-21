package com.portalfacturacion.application.service;

import com.portalfacturacion.configuration.CorreoProperties;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Resuelve la imagen del logo para embeberla dentro del correo (cid inline),
 * de modo que el logo se vea sin depender de que el cliente pueda descargar
 * imagenes remotas (Gmail/Outlook bloquean o no alcanzan URLs locales o internas).
 */
@Service
public class LogoProvider {

  public static final String CID = "logo-oso-despierto";

  private static final Logger log = LoggerFactory.getLogger(LogoProvider.class);

  private static final String[] CLASSPATH_CANDIDATOS = {
    "static/images/logo.png",
    "static/images/logo.jpg",
    "static/images/logo.jpeg",
    "images/logo.png",
    "logo.png"
  };

  private final CorreoProperties props;
  private byte[] cache;
  private boolean cacheResuelto;

  public LogoProvider(CorreoProperties props) {
    this.props = props;
  }

  public synchronized byte[] bytes() {
    if (!cacheResuelto) {
      cache = cargar();
      cacheResuelto = true;
    }
    return cache;
  }

  public boolean disponible() {
    byte[] data = bytes();
    return data != null && data.length > 0;
  }

  public String contentType() {
    byte[] data = bytes();
    if (data == null || data.length < 4) {
      return "image/png";
    }
    if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
      return "image/png";
    }
    if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
      return "image/jpeg";
    }
    if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
      return "image/gif";
    }
    if (data[0] == 'W' && data[1] == 'E' && data[2] == 'B' && data[3] == 'P') {
      return "image/webp";
    }
    return "image/png";
  }

  private byte[] cargar() {
    for (String recurso : CLASSPATH_CANDIDATOS) {
      try (InputStream in = getClass().getClassLoader().getResourceAsStream(recurso)) {
        if (in == null) {
          continue;
        }
        byte[] data = in.readAllBytes();
        if (data.length > 0) {
          log.info("Logo de correo cargado del classpath: {} ({} bytes)", recurso, data.length);
          return data;
        }
      } catch (Exception e) {
        log.warn("No se pudo leer el logo desde el recurso {}: {}", recurso, e.getMessage());
      }
    }
    return descargarDeUrl();
  }

  private byte[] descargarDeUrl() {
    String url = props.getRutaLogo();
    if (url == null || url.isBlank() || !url.startsWith("http")) {
      return null;
    }
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) URI.create(url.trim()).toURL().openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.setInstanceFollowRedirects(true);
      conn.setRequestProperty("User-Agent", "portal-facturacion-mail");
      int codigo = conn.getResponseCode();
      if (codigo < 200 || codigo > 299) {
        log.warn("No se pudo descargar el logo (HTTP {}) desde {}", codigo, url);
        return null;
      }
      try (InputStream in = conn.getInputStream()) {
        byte[] data = in.readAllBytes();
        if (data.length > 0) {
          log.info("Logo de correo descargado desde {} ({} bytes)", url, data.length);
        }
        return data;
      }
    } catch (Exception e) {
      log.warn("Fallo descargando el logo desde {}: {}", url, e.getMessage());
      return null;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }
}
