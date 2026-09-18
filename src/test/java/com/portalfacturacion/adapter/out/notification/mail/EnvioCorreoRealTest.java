package com.portalfacturacion.adapter.out.notification.mail;

import com.portalfacturacion.application.service.CorreoPreparado;
import com.portalfacturacion.application.service.PlantillaCorreoService;
import com.portalfacturacion.configuration.CorreoConfig;
import com.portalfacturacion.configuration.CorreoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class EnvioCorreoRealTest {

  @Test
  void envioRealConLayout() {
    String username = System.getenv("CORREO_USERNAME");
    String password = System.getenv("CORREO_PASSWORD");
    String destino = System.getenv("CORREO_DESTINO");
    assumeTrue(username != null && !username.isBlank()
        && password != null && !password.isBlank()
        && destino != null && !destino.isBlank(),
      "Sin credenciales: exporta CORREO_USERNAME, CORREO_PASSWORD y CORREO_DESTINO");

    CorreoProperties props = new CorreoProperties();
    props.setHost("smtp.gmail.com");
    props.setPort(587);
    props.setUsername(username);
    props.setPassword(password);
    props.setRemitente(username);
    props.setRutaLayouts(System.getenv().getOrDefault("CORREO_LAYOUTS_DIR",
      "/Users/joser.vazquez/config-apps/portal-facturacion"));
    props.setRutaLogo(System.getenv().getOrDefault("CORREO_RUTA_LOGO",
      "http://localhost:8080/api/v1/images/logo.png"));

    JavaMailSender sender = new CorreoConfig().javaMailSender(props);
    PlantillaCorreoService plantillas = new PlantillaCorreoService(props);
    CorreoPreparado correo = plantillas.construirCorreo("correo_factura.html",
      "titulo=" + destino, "folio=PRUEBA-001");

    SmtpCorreoAdapter adapter = new SmtpCorreoAdapter(sender, props);
    assertDoesNotThrow(() -> adapter.enviarTextoPlano(destino, correo.asunto(), correo.contenido()));
  }
}
