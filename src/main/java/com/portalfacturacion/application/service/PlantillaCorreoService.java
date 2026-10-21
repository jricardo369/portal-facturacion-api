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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlantillaCorreoService {

  private static final Logger log = LoggerFactory.getLogger(PlantillaCorreoService.class);

  private final CorreoProperties props;
  private final LogoProvider logos;

  public PlantillaCorreoService(CorreoProperties props) {
    this(props, new LogoProvider(props));
  }

  @Autowired
  public PlantillaCorreoService(CorreoProperties props, LogoProvider logos) {
    this.props = props;
    this.logos = logos;
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
    String rutaLogo = props.getRutaLogo() != null ? props.getRutaLogo() : "";
    if (props.isLogoInline() && logos.disponible()) {
      // Se embebe el logo en el correo (cid) para que se vea sin descargar imagenes remotas.
      // Normaliza layouts antiguos del tipo src="${logo}images/logo.png".
      plantilla = plantilla.replaceAll("(\\$\\{logo\\})images/[^\"')\\s>]*", "$1");
      valores.putIfAbsent("logo", "cid:" + LogoProvider.CID);
    } else {
      valores.putIfAbsent("logo", resolverLogo(plantilla, rutaLogo));
    }
    String resultado = plantilla;
    for (Map.Entry<String, String> entry : valores.entrySet()) {
      String valor = entry.getValue() != null ? entry.getValue() : "";
      resultado = resultado.replace("${" + entry.getKey() + "}", valor);
    }
    return resultado;
  }

  /**
   * Resuelve el valor de ${logo} para que funcione con las dos convenciones de ruta-logo,
   * sin necesidad de editar los layouts ya subidos al servidor:
   * - layout nuevo: src="${logo}"            + ruta-logo = URL completa de la imagen
   * - layout anterior: src="${logo}images/logo.png" + ruta-logo = URL completa o base
   * Devuelve la URL final de la imagen.
   */
  private String resolverLogo(String plantilla, String rutaLogo) {
    if (rutaLogo == null || rutaLogo.isBlank()) {
      return "";
    }
    String logo = rutaLogo.trim();
    boolean layoutUsaBase = plantilla.contains("${logo}images/");
    if (layoutUsaBase) {
      // El layout concatena "images/logo.png": hay que inyectar la base (sin el archivo).
      if (logo.endsWith("images/logo.png")) {
        return logo.substring(0, logo.length() - "images/logo.png".length());
      }
      if (logo.endsWith("logo.png")) {
        return logo.substring(0, logo.length() - "logo.png".length());
      }
      if (logo.endsWith("images/")) {
        return logo.substring(0, logo.length() - "images/".length());
      }
      return logo.endsWith("/") ? logo : logo + "/";
    }
    // El layout usa ${logo} a secas: necesita la URL completa de la imagen.
    if (logo.endsWith("/")) {
      return logo + "images/logo.png";
    }
    return logo;
  }

  public String leerLayout(String nombreLayout) {
    if (nombreLayout == null || nombreLayout.isBlank()) {
      throw new IllegalArgumentException("nombreLayout es requerido");
    }
    String rutaLayouts = props.getRutaLayouts();
    if (rutaLayouts != null && !rutaLayouts.isBlank()) {
      Path base = Paths.get(rutaLayouts);
      Path candidato = base.resolve(nombreLayout);
      if (Files.notExists(candidato) && !nombreLayout.contains(".")) {
        candidato = base.resolve(nombreLayout + ".html");
      }
      try {
        String contenido = Files.readString(candidato, StandardCharsets.UTF_8);
        log.debug("Layout cargado desde disco: {}", candidato);
        return contenido;
      } catch (Exception e) {
        log.warn("No se pudo leer el layout {} en {}. Se intentara el classpath. error={}",
          nombreLayout, candidato, e.getMessage());
      }
    } else {
      log.warn("app.correo.ruta-layouts no esta configurada. Se usara el classpath.");
    }
    String classpath = leerDelClasspath(nombreLayout);
    if (classpath != null) {
      return classpath;
    }
    throw new IllegalArgumentException("No se pudo leer el layout de correo: " + nombreLayout
      + " (ni en ruta-layouts ni en classpath:/html-mails/)");
  }

  private String leerDelClasspath(String nombreLayout) {
    String[] candidatos = nombreLayout.contains(".")
      ? new String[] { "html-mails/" + nombreLayout }
      : new String[] { "html-mails/" + nombreLayout + ".html", "html-mails/" + nombreLayout };
    for (String recurso : candidatos) {
      try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(recurso)) {
        if (in == null) {
          continue;
        }
        String contenido = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        log.debug("Layout cargado desde classpath: {}", recurso);
        return contenido;
      } catch (Exception e) {
        log.warn("Fallo leyendo layout desde classpath {}: {}", recurso, e.getMessage());
      }
    }
    return null;
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
