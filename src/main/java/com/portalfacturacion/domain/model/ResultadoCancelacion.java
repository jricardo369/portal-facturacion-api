package com.portalfacturacion.domain.model;

public class ResultadoCancelacion {

  private String id;
  private String uuid;
  private String status;
  private String cancellationStatus;

  public ResultadoCancelacion() {
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getUuid() { return uuid; }
  public void setUuid(String uuid) { this.uuid = uuid; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getCancellationStatus() { return cancellationStatus; }
  public void setCancellationStatus(String cancellationStatus) { this.cancellationStatus = cancellationStatus; }

  public boolean aceptada() {
    return "accepted".equalsIgnoreCase(cancellationStatus)
        || "canceled".equalsIgnoreCase(status);
  }
}
