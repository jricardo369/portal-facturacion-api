package com.portalfacturacion.configuration;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class CorreoConfig {

  private static final Logger log = LoggerFactory.getLogger(CorreoConfig.class);

  @Bean
  public JavaMailSender javaMailSender(CorreoProperties props) {
    if (props.getUsername() == null || props.getUsername().isBlank()
        || props.getUsername().contains("CAMBIAR")
        || props.getPassword() == null || props.getPassword().isBlank()
        || props.getPassword().contains("CAMBIAR")) {
      log.warn("Credenciales de correo sin configurar en configuracion-general.yml (username/password). "
        + "Los envios se omitiran hasta corregirlo.");
    }
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(props.getHost());
    sender.setPort(props.getPort());
    if (props.getUsername() != null && !props.getUsername().isBlank()) {
      sender.setUsername(props.getUsername());
    }
    if (props.getPassword() != null && !props.getPassword().isBlank()) {
      sender.setPassword(props.getPassword());
    }
    Properties javaMailProps = sender.getJavaMailProperties();
    javaMailProps.put("mail.smtp.auth", String.valueOf(props.isAuth()));
    javaMailProps.put("mail.smtp.starttls.enable", String.valueOf(props.isStarttls()));
    javaMailProps.put("mail.smtp.connectiontimeout", String.valueOf(props.getTimeoutMs()));
    javaMailProps.put("mail.smtp.timeout", String.valueOf(props.getTimeoutMs()));
    javaMailProps.put("mail.smtp.writetimeout", String.valueOf(props.getTimeoutMs()));
    return sender;
  }
}
