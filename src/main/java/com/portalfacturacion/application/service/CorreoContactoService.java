package com.portalfacturacion.application.service;

import com.portalfacturacion.application.port.in.contacto.ActualizarConfiguracionUseCase;
import com.portalfacturacion.application.port.in.contacto.EnviarCorreoContactoUseCase;
import com.portalfacturacion.application.port.in.contacto.ObtenerConfiguracionesUseCase;
import com.portalfacturacion.application.port.in.contacto.SolicitudSoporteCommand;
import com.portalfacturacion.application.port.out.notification.CorreoPort;
import com.portalfacturacion.application.port.out.persistence.ConfiguracionPort;
import com.portalfacturacion.domain.model.Configuracion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CorreoContactoService implements EnviarCorreoContactoUseCase, ActualizarConfiguracionUseCase,
    ObtenerConfiguracionesUseCase {

  public static final String CLAVE_CORREO_DUDAS = "correo_dudas_aclaraciones";
  public static final String CORREO_DEFECTO = "osodespiertocoffee@gmail.com";
  private static final String LAYOUT_DUDA = "correo_duda.html";
  private static final String LAYOUT_ACLARACION = "correo_aclaracion.html";

  private static final Logger log = LoggerFactory.getLogger(CorreoContactoService.class);

  private final ConfiguracionPort configuracionPort;
  private final PlantillaCorreoService plantillas;
  private final CorreoPort correo;

  public CorreoContactoService(ConfiguracionPort configuracionPort,
      PlantillaCorreoService plantillas, CorreoPort correo) {
    this.configuracionPort = configuracionPort;
    this.plantillas = plantillas;
    this.correo = correo;
  }

  @Override
  public void enviar(SolicitudSoporteCommand command) {
    if (command == null || command.motivo() == null) {
      throw new IllegalArgumentException("La solicitud de soporte y su motivo son requeridos");
    }
    String destinatario = resolverDestinatario();
    Map<String, String> parametros = base(command);
    SolicitudSoporteCommand.Motivo motivo = command.motivo();

    String asunto;
    String layout;
    switch (motivo) {
      case DUDA -> {
        asunto = "El cliente tiene una duda";
        layout = LAYOUT_DUDA;
      }
      case ACLARACION -> {
        asunto = "Aclaración de una factura";
        layout = LAYOUT_ACLARACION;
        parametros.put("fecha", nvl(command.fecha()));
        parametros.put("hora", nvl(command.hora()));
        parametros.put("total", nvl(command.total()));
      }
      default -> throw new IllegalArgumentException("Motivo no soportado: " + motivo);
    }

    String contenido = plantillas.construirContenido(layout, parametros);
    correo.enviarTextoPlano(destinatario, asunto, contenido);
    log.info("Correo de contacto enviado. motivo={} destinatario={}", motivo, destinatario);
  }

  @Override
  public List<Configuracion> obtenerTodas() {
    return configuracionPort.findAll();
  }

  @Override
  public Configuracion actualizar(String clave, String valor, String descripcion, Boolean activo) {
    if (clave == null || clave.isBlank()) {
      throw new IllegalArgumentException("La clave de configuracion es requerida");
    }
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException("El valor de la configuracion es requerido");
    }
    Configuracion actual = configuracionPort.findByClave(clave.trim())
        .orElseThrow(() -> new IllegalArgumentException("No existe una configuracion con la clave: " + clave));
    Configuracion actualizada = new Configuracion(
        actual.id(),
        actual.clave(),
        valor.trim(),
        descripcion != null && !descripcion.isBlank() ? descripcion.trim() : actual.descripcion(),
        activo != null ? activo : actual.activo(),
        null,
        null);
    return configuracionPort.save(actualizada);
  }

  public String resolverDestinatario() {
    return configuracionPort.findByClave(CLAVE_CORREO_DUDAS)
        .filter(c -> c.activo() == null || c.activo())
        .map(Configuracion::valor)
        .filter(v -> !v.isBlank())
        .orElse(CORREO_DEFECTO);
  }

  private Map<String, String> base(SolicitudSoporteCommand command) {
    Map<String, String> parametros = new HashMap<>();
    parametros.put("nombre", nvl(command.nombre()));
    parametros.put("descripcion", nvl(command.descripcion()));
    return parametros;
  }

  private String nvl(String valor) {
    return valor == null ? "" : valor;
  }
}
