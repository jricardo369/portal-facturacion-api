package com.portalfacturacion.application.service;

import com.portalfacturacion.configuration.CorreoProperties;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HeaderCorreoProvider {

  public static final String CID = "header-correo";

  private static final Logger log = LoggerFactory.getLogger(HeaderCorreoProvider.class);

  private static final String[] CLASSPATH_CANDIDATOS = {
    "static/images/headerCorreo.png",
    "images/headerCorreo.png",
    "headerCorreo.png"
  };

  private final CorreoProperties props;
  private byte[] cache;
  private boolean cacheResuelto;

  public HeaderCorreoProvider(CorreoProperties props) {
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
          log.info("Header de correo cargado del classpath: {} ({} bytes)", recurso, data.length);
          return data;
        }
      } catch (Exception e) {
        log.warn("No se pudo leer el header de correo desde el recurso {}: {}", recurso, e.getMessage());
      }
    }
    return null;
  }
}
