package com.portalfacturacion.adapter.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "configuracion")
public class ConfiguracionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "clave", nullable = false, unique = true, length = 64)
  private String clave;

  @Column(name = "valor", nullable = false, length = 255)
  private String valor;

  @Column(name = "descripcion", length = 255)
  private String descripcion;

  @Column(name = "activo", nullable = false)
  private Boolean activo = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getClave() { return clave; }
  public void setClave(String clave) { this.clave = clave; }
  public String getValor() { return valor; }
  public void setValor(String valor) { this.valor = valor; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public Boolean getActivo() { return activo; }
  public void setActivo(Boolean activo) { this.activo = activo; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

