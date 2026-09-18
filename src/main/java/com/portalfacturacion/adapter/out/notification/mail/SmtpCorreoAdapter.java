package com.portalfacturacion.adapter.out.notification.mail;

import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.configuration.CorreoProperties;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SmtpCorreoAdapter implements CorreoPort {

  private static final Logger log = LoggerFactory.getLogger(SmtpCorreoAdapter.class);

  private final JavaMailSender mailSender;
  private final CorreoProperties props;

  public SmtpCorreoAdapter(JavaMailSender mailSender, CorreoProperties props) {
    this.mailSender = mailSender;
    this.props = props;
  }

  @Async("correoExecutor")
  @Override
  public void enviarTextoPlano(String destinatario, String asunto, String contenido) {
    try {
      if (!props.isConfigured()) {
        log.warn("Correo no configurado. Se omite envio a={}", destinatario);
        return;
      }
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(props.remitenteEfectivo(), props.getNombreRemitente());
      helper.setTo(destinatario);
      helper.setSubject(asunto);
      helper.setText(contenido, true);
      mailSender.send(message);
      log.info("Correo enviado de={} a={} asunto={}", props.remitenteEfectivo(), destinatario, asunto);
    } catch (Exception e) {
      log.error("Fallo envio de correo a={} asunto={}. No se interrumpe el proceso. error={}",
        destinatario, asunto, e.getMessage(), e);
    }
  }

  @Async("correoExecutor")
  @Override
  public void enviarTextoPlanoConAdjuntos(String destinatario, String asunto, String contenido, List<File> adjuntos) {
    try {
      if (!props.isConfigured()) {
        log.warn("Correo no configurado. Se omite envio con adjuntos a={}", destinatario);
        return;
      }
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(props.remitenteEfectivo(), props.getNombreRemitente());
      helper.setTo(destinatario);
      helper.setSubject(asunto);
      helper.setText(contenido, true);
      if (adjuntos != null) {
        for (File adjunto : adjuntos) {
          if (adjunto != null && adjunto.exists() && adjunto.isFile()) {
            helper.addAttachment(adjunto.getName(), adjunto);
          } else {
            log.warn("Adjunto omitido (no existe): {}", adjunto);
          }
        }
      }
      mailSender.send(message);
      log.info("Correo con adjuntos enviado de={} a={} asunto={}", props.remitenteEfectivo(), destinatario, asunto);
    } catch (Exception e) {
      log.error("Fallo envio de correo con adjuntos a={} asunto={}. No se interrumpe el proceso. error={}",
        destinatario, asunto, e.getMessage(), e);
    }
  }
}
