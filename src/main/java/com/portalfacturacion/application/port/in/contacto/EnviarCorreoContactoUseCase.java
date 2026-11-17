package com.portalfacturacion.application.port.in.contacto;

public interface EnviarCorreoContactoUseCase {

  void enviar(SolicitudSoporteCommand command);
}
