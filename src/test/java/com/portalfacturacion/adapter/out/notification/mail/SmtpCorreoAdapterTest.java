package com.portalfacturacion.adapter.out.notification.mail;

import com.portalfacturacion.application.service.CorreoPreparado;
import com.portalfacturacion.application.service.PlantillaCorreoService;
import com.portalfacturacion.configuration.CorreoProperties;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpCorreoAdapterTest {

  @TempDir
  Path temp;

  @Mock
  JavaMailSender mailSender;

  private CorreoProperties props;
  private SmtpCorreoAdapter adapter;

  @BeforeEach
  void setup() {
    props = new CorreoProperties();
    props.setHost("smtp.gmail.com");
    props.setPort(587);
    props.setRemitente("no-reply@tudominio.com");
    props.setNombreRemitente("Portal Facturacion");
    props.setRutaLayouts(temp.toString());
    props.setRutaLogo("http://localhost:8080/api/v1/images/logo.png");
    adapter = new SmtpCorreoAdapter(mailSender, props);
    lenient().when(mailSender.createMimeMessage())
      .thenAnswer(inv -> new MimeMessage(jakarta.mail.Session.getInstance(new Properties())));
  }

  @Test
  void enviarCorreoDesdeLayout_construyeYEnvia() throws Exception {
    Files.writeString(temp.resolve("correo_factura.html"),
      "<img src=\"${logo}\"><h1>${titulo}</h1><p>Folio ${folio}</p>", StandardCharsets.UTF_8);
    PlantillaCorreoService plantillas = new PlantillaCorreoService(props);
    CorreoPreparado correo = plantillas.construirCorreo("correo_factura.html",
      "titulo=Factura generada", "folio=F-123");

    adapter.enviarTextoPlano("cliente@correo.com", correo.asunto(), correo.contenido());

    ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailSender).send(captor.capture());
    MimeMessage enviado = captor.getValue();
    assertEquals("Factura generada", enviado.getSubject());
    jakarta.mail.internet.InternetAddress de =
      (jakarta.mail.internet.InternetAddress) enviado.getFrom()[0];
    assertEquals("no-reply@tudominio.com", de.getAddress());
    assertEquals("Portal Facturacion", de.getPersonal());
    String cuerpo = (String) enviado.getContent();
    assertTrue(cuerpo.contains("F-123"));
    assertTrue(cuerpo.contains("http://localhost:8080/api/v1/images/logo.png"));
    assertFalse(cuerpo.contains("${"));
  }

  @Test
  void enviarTextoPlanoConAdjuntos_incluyeArchivo() throws Exception {
    Path pdf = temp.resolve("factura.pdf");
    Files.writeString(pdf, "contenido-falso", StandardCharsets.UTF_8);

    adapter.enviarTextoPlanoConAdjuntos("cliente@correo.com", "Tu factura",
      "<p>Hola</p>", List.of(pdf.toFile()));

    ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailSender).send(captor.capture());
    jakarta.mail.internet.MimeMultipart multipart =
      (jakarta.mail.internet.MimeMultipart) captor.getValue().getContent();
    assertEquals(2, multipart.getCount());
    assertEquals("factura.pdf", multipart.getBodyPart(1).getFileName());
  }

  @Test
  void fromUsaUsernameCuandoExiste() throws Exception {
    props.setUsername("ventas@gmail.com");
    adapter.enviarTextoPlano("cliente@correo.com", "Asunto", "<p>Hola</p>");
    ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailSender).send(captor.capture());
    jakarta.mail.internet.InternetAddress de =
      (jakarta.mail.internet.InternetAddress) captor.getValue().getFrom()[0];
    assertEquals("ventas@gmail.com", de.getAddress());
  }

  @Test
  void falloEnvio_noInterrumpeProceso() {
    doThrow(new RuntimeException("SMTP caido")).when(mailSender).send(any(MimeMessage.class));
    assertDoesNotThrow(() ->
      adapter.enviarTextoPlano("cliente@correo.com", "Asunto", "<p>Hola</p>"));
    assertDoesNotThrow(() ->
      adapter.enviarTextoPlanoConAdjuntos("cliente@correo.com", "Asunto", "<p>Hola</p>", List.of()));
  }
}
