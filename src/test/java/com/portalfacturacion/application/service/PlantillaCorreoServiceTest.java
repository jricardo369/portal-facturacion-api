package com.portalfacturacion.application.service;

import com.portalfacturacion.configuration.CorreoProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class PlantillaCorreoServiceTest {

  @TempDir
  Path layouts;

  private CorreoProperties props;
  private PlantillaCorreoService service;

  @BeforeEach
  void setup() throws Exception {
    props = new CorreoProperties();
    props.setRutaLayouts(layouts.toString());
    props.setRutaLogo("http://localhost:8080/api/v1/images/logo.png");
    props.setLogoInline(false);
    props.setRemitente("base@tudominio.com");
    props.setNombreRemitente("Base");
    service = new PlantillaCorreoService(props);
    Files.writeString(layouts.resolve("correo_factura.html"),
      "<img src=\"${logo}\"><p>${folio}</p><p>${asunto}</p>", StandardCharsets.UTF_8);
  }

  @Test
  void construirContenido_reemplazaPlaceholdersYLogo() {
    String html = service.construirContenido("correo_factura.html", "folio=ABC-123");
    assertTrue(html.contains("ABC-123"));
    assertTrue(html.contains("http://localhost:8080/api/v1/images/logo.png"));
    assertFalse(html.contains("${logo}"));
    assertFalse(html.contains("${folio}"));
  }

  @Test
  void construirContenido_aceptaMapa() {
    String html = service.construirContenido("correo_factura.html", Map.of("folio", "F-9"));
    assertTrue(html.contains("F-9"));
  }

  @Test
  void construirCorreo_remitenteSiempreDelArchivo() {
    CorreoPreparado correo = service.construirCorreo("correo_factura.html",
      "titulo=Factura generada", "remitente=otro@tudominio.com",
      "nombre-remitente=Otro", "folio=77");
    assertEquals("base@tudominio.com", correo.remitente());
    assertEquals("Base", correo.nombreRemitente());
    assertEquals("Factura generada", correo.asunto());
    assertTrue(correo.contenido().contains("77"));
    assertTrue(correo.contenido().contains("http://localhost:8080/api/v1/images/logo.png"));
  }

  @Test
  void construirCorreo_usaRemitentePorDefecto() {
    CorreoPreparado correo = service.construirCorreo("correo_factura.html", "titulo=Hola", "folio=1");
    assertEquals("base@tudominio.com", correo.remitente());
    assertEquals("Base", correo.nombreRemitente());
    assertEquals("Hola", correo.asunto());
  }

  @Test
  void leerLayout_inexistente_lanzaExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> service.leerLayout("no_existe.html"));
  }

  @Test
  void leerLayout_caeAClasspathSiLaRutaNoExiste() {
    props.setRutaLayouts(layouts.resolve("no-existe").toString());
    String html = service.leerLayout("correo_factura.html");
    assertTrue(html.contains("${logo}"));
  }

  @Test
  void logoConLayoutViejoYUrlCompleta_NoDuplicaRuta() throws Exception {
    Files.writeString(layouts.resolve("viejo.html"),
      "<img src=\"${logo}images/logo.png\">", StandardCharsets.UTF_8);
    props.setRutaLogo("http://ec2-54-215-12-132.us-west-1.compute.amazonaws.com/api/v1/images/logo.png");
    String html = service.construirContenido("viejo.html");
    assertTrue(html.contains("src=\"http://ec2-54-215-12-132.us-west-1.compute.amazonaws.com/api/v1/images/logo.png\""));
  }

  @Test
  void logoConLayoutViejoYBase_ConstruyeUrlCompleta() throws Exception {
    Files.writeString(layouts.resolve("viejo.html"),
      "<img src=\"${logo}images/logo.png\">", StandardCharsets.UTF_8);
    props.setRutaLogo("http://localhost:18080/api/v1/");
    String html = service.construirContenido("viejo.html");
    assertTrue(html.contains("src=\"http://localhost:18080/api/v1/images/logo.png\""));
  }

  @Test
  void logoConLayoutNuevoYBase_ConstruyeUrlCompleta() throws Exception {
    Files.writeString(layouts.resolve("nuevo.html"),
      "<img src=\"${logo}\">", StandardCharsets.UTF_8);
    props.setRutaLogo("http://localhost:18080/api/v1/");
    String html = service.construirContenido("nuevo.html");
    assertTrue(html.contains("src=\"http://localhost:18080/api/v1/images/logo.png\""));
  }

  @Test
  void logoConLayoutNuevoYUrlCompleta_NoModificaLaUrl() throws Exception {
    Files.writeString(layouts.resolve("nuevo.html"),
      "<img src=\"${logo}\">", StandardCharsets.UTF_8);
    props.setRutaLogo("http://localhost:18080/api/v1/images/logo.png");
    String html = service.construirContenido("nuevo.html");
    assertTrue(html.contains("src=\"http://localhost:18080/api/v1/images/logo.png\""));
  }

  @Test
  void logoInline_UsaContentIdEnVezDeUrl() throws Exception {
    props.setLogoInline(true);
    Files.writeString(layouts.resolve("nuevo.html"),
      "<img src=\"${logo}\">", StandardCharsets.UTF_8);
    String html = service.construirContenido("nuevo.html");
    assertTrue(html.contains("src=\"cid:logo-oso-despierto\""));
    assertFalse(html.contains("http://localhost:18080"));
  }

  @Test
  void logoInline_NormalizaLayoutAntiguo() throws Exception {
    props.setLogoInline(true);
    Files.writeString(layouts.resolve("viejo.html"),
      "<img src=\"${logo}images/logo.png\">", StandardCharsets.UTF_8);
    String html = service.construirContenido("viejo.html");
    assertTrue(html.contains("src=\"cid:logo-oso-despierto\""));
  }
}
