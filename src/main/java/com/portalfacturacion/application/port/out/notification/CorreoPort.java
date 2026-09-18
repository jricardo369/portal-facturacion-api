package com.portalfacturacion.application.port.out.notification;

import java.io.File;
import java.util.List;

public interface CorreoPort {

  void enviarTextoPlano(String destinatario, String asunto, String contenido);

  void enviarTextoPlanoConAdjuntos(String destinatario, String asunto, String contenido, List<File> adjuntos);
}
