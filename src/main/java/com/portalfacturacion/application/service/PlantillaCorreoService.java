package com.portalfacturacion.application.service;

import com.portalfacturacion.configuration.CorreoProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PlantillaCorreoService {

  private static final Logger log = LoggerFactory.getLogger(PlantillaCorreoService.class);

  private final CorreoProperties props;

  public PlantillaCorreoService(CorreoProperties props) {
    this.props = props;
  }

  public CorreoPreparado construirCorreo(String nombreLayout, String... parametros) {
    return construirCorreo(nombreLayout, aMapa(parametros));
  }

  public CorreoPreparado construirCorreo(String nombreLayout, Map<String, String> parametros) {
    Map<String, String> valores = new HashMap<>();
    if (parametros != null) {
      valores.putAll(parametros);
    }
    String remitente = props.remitenteEfectivo() != null ? props.remitenteEfectivo() : "";
    String nombreRemitente = props.getNombreRemitente() != null ? props.getNombreRemitente() : "";
    extraer(valores, "", "remitente", "from", "de");
    extraer(valores, "", "nombre-remitente", "nombreRemitente", "nombre", "fromName");
    String asunto = extraer(valores, "", "asunto", "titulo", "subject", "title");
    if (asunto.isBlank()) {
      log.warn("Layout {} sin asunto/titulo en parametros", nombreLayout);
    }
    valores.put("remitente", remitente);
    valores.put("nombre-remitente", nombreRemitente);
    valores.put("nombreRemitente", nombreRemitente);
    valores.put("asunto", asunto);
    valores.putIfAbsent("titulo", asunto);
    String contenido = construirContenido(nombreLayout, valores);
    return new CorreoPreparado(remitente, nombreRemitente, asunto, contenido);
  }

  public String construirContenido(String nombreLayout, String... parametros) {
    return construirContenido(nombreLayout, aMapa(parametros));
  }

  public String construirContenido(String nombreLayout, Map<String, String> parametros) {
    String plantilla = leerLayout(nombreLayout);
    Map<String, String> valores = new HashMap<>();
    if (parametros != null) {
      valores.putAll(parametros);
    }
    valores.putIfAbsent("logo", props.getRutaLogo() != null ? props.getRutaLogo() : "");
    String resultado = plantilla;
    for (Map.Entry<String, String> entry : valores.entrySet()) {
      String valor = entry.getValue() != null ? entry.getValue() : "";
      resultado = resultado.replace("${" + entry.getKey() + "}", valor);
    }
    return resultado;
  }

  public String leerLayout(String nombreLayout) {
    if (nombreLayout == null || nombreLayout.isBlank()) {
      throw new IllegalArgumentException("nombreLayout es requerido");
    }
    if (props.getRutaLayouts() == null || props.getRutaLayouts().isBlank()) {
      throw new IllegalStateException("app.correo.ruta-layouts no esta configurada");
    }
    Path base = Paths.get(props.getRutaLayouts());
    Path candidato = base.resolve(nombreLayout);
    if (Files.notExists(candidato) && !nombreLayout.contains(".")) {
      candidato = base.resolve(nombreLayout + ".html");
    }
    try {
      String contenido = Files.readString(candidato, StandardCharsets.UTF_8);
      log.debug("Layout cargado: {}", candidato);
      return contenido;
    } catch (Exception e) {
      throw new IllegalArgumentException("No se pudo leer el layout de correo: " + candidato, e);
    }
  }

  private String extraer(Map<String, String> valores, String defecto, String... claves) {
    for (String clave : claves) {
      String v = valores.remove(clave);
      if (v != null && !v.isBlank()) {
        return v;
      }
    }
    return defecto;
  }

  private Map<String, String> aMapa(String... parametros) {
    Map<String, String> mapa = new HashMap<>();
    if (parametros == null) {
      return mapa;
    }
    for (String p : parametros) {
      if (p == null || p.isBlank() || !p.contains("=")) {
        continue;
      }
      int idx = p.indexOf('=');
      String clave = p.substring(0, idx).trim();
      String valor = p.substring(idx + 1).trim();
      if (!clave.isBlank()) {
        mapa.put(clave, valor);
      }
    }
    return mapa;
  }
}
