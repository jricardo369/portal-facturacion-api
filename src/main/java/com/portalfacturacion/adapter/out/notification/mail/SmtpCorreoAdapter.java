package com.portalfacturacion.adapter.out.notification.mail;

import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.application.service.LogoProvider;
import com.portalfacturacion.configuration.CorreoProperties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SmtpCorreoAdapter implements CorreoPort {

  private static final Logger log = LoggerFactory.getLogger(SmtpCorreoAdapter.class);

  private final JavaMailSender mailSender;
  private final CorreoProperties props;
  private final LogoProvider logos;

  public SmtpCorreoAdapter(JavaMailSender mailSender, CorreoProperties props) {
    this(mailSender, props, new LogoProvider(props));
  }

  @Autowired
  public SmtpCorreoAdapter(JavaMailSender mailSender, CorreoProperties props, LogoProvider logos) {
    this.mailSender = mailSender;
    this.props = props;
    this.logos = logos;
  }

  @Async("correoExecutor")
  @Override
  public void enviarTextoPlano(String destinatario, String asunto, String contenido) {
    enviar(destinatario, asunto, contenido, List.of());
  }

  @Async("correoExecutor")
  @Override
  public void enviarTextoPlanoConAdjuntos(String destinatario, String asunto, String contenido, List<File> adjuntos) {
    enviar(destinatario, asunto, contenido, adjuntos == null ? List.of() : adjuntos);
  }

  private void enviar(String destinatario, String asunto, String contenido, List<File> adjuntos) {
    try {
      if (!props.isConfigured()) {
        log.warn("Correo no configurado. Se omite envio a={}", destinatario);
        return;
      }
      MimeMessage message = construirMensaje(destinatario, asunto, contenido, adjuntos);
      mailSender.send(message);
      log.info("Correo enviado de={} a={} asunto={}", props.remitenteEfectivo(), destinatario, asunto);
    } catch (Exception e) {
      log.error("Fallo envio de correo a={} asunto={}. No se interrumpe el proceso. error={}",
        destinatario, asunto, e.getMessage(), e);
    }
  }

  private MimeMessage construirMensaje(String destinatario, String asunto, String contenido, List<File> adjuntos)
    throws Exception {
    MimeMessage message = mailSender.createMimeMessage();
    String nombreRemitente = props.getNombreRemitente() != null ? props.getNombreRemitente() : "";
    message.setFrom(nombreRemitente.isBlank()
      ? new InternetAddress(props.remitenteEfectivo())
      : new InternetAddress(props.remitenteEfectivo(), nombreRemitente));
    message.setRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(destinatario));
    message.setSubject(asunto != null ? asunto : "", "UTF-8");

    byte[] logo = logoInline(contenido);
    if (logo != null) {
      MimeMultipart mixed = new MimeMultipart("mixed");
      MimeMultipart related = new MimeMultipart("related");

      MimeBodyPart html = new MimeBodyPart();
      html.setContent(contenido != null ? contenido : "", "text/html; charset=UTF-8");
      related.addBodyPart(html);

      MimeBodyPart imagen = new MimeBodyPart();
      imagen.setContent(logo, logos.contentType());
      imagen.setHeader("Content-Disposition", "inline; filename=\"logo.png\"");
      imagen.setHeader("Content-ID", "<" + LogoProvider.CID + ">");
      related.addBodyPart(imagen);

      MimeBodyPart relatedPart = new MimeBodyPart();
      relatedPart.setContent(related);
      mixed.addBodyPart(relatedPart);

      for (File adjunto : adjuntos) {
        if (adjunto == null || !adjunto.exists() || !adjunto.isFile()) {
          log.warn("Adjunto omitido (no existe): {}", adjunto);
          continue;
        }
        MimeBodyPart parte = new MimeBodyPart();
        parte.setContent(Files.readAllBytes(adjunto.toPath()), contentType(adjunto.getName()));
        parte.setHeader("Content-Disposition", "attachment; filename=\"" + adjunto.getName() + "\"");
        mixed.addBodyPart(parte);
      }
      message.setContent(mixed);
      return message;
    }

    MimeMessageHelper helper = new MimeMessageHelper(message, !adjuntos.isEmpty(), "UTF-8");
    helper.setFrom(props.remitenteEfectivo(), nombreRemitente);
    helper.setTo(destinatario);
    helper.setSubject(asunto != null ? asunto : "");
    helper.setText(contenido != null ? contenido : "", true);
    for (File adjunto : adjuntos) {
      if (adjunto == null || !adjunto.exists() || !adjunto.isFile()) {
        log.warn("Adjunto omitido (no existe): {}", adjunto);
        continue;
      }
      helper.addAttachment(adjunto.getName(), adjunto);
    }
    return message;
  }

  private byte[] logoInline(String contenido) {
    if (!props.isLogoInline() || contenido == null || !contenido.contains("cid:" + LogoProvider.CID)) {
      return null;
    }
    byte[] data = logos.bytes();
    if (data == null || data.length == 0) {
      log.warn("El layout referencia el logo embebido (cid) pero no se pudo cargar la imagen");
      return null;
    }
    return data;
  }

  private String contentType(String nombreArchivo) {
    String nombre = nombreArchivo != null ? nombreArchivo.toLowerCase() : "";
    if (nombre.endsWith(".pdf")) {
      return "application/pdf";
    }
    if (nombre.endsWith(".xml")) {
      return "application/xml";
    }
    if (nombre.endsWith(".png")) {
      return "image/png";
    }
    if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    return "application/octet-stream";
  }
}
