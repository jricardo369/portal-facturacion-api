package com.portalfacturacion.domain.model;

import java.time.Instant;

public class Usuario {
  private Long id;
  private String usuario;
  private String password;
  private String nombre;
  private String direccion;
  private String telefono;
  private TipoUsuario tipo;
  private Boolean activo;
  private Instant createdAt;
  private Instant updatedAt;

  public Usuario() {
  }

  public Usuario(Long id, String usuario, String password, String nombre, String direccion,
                 String telefono, TipoUsuario tipo, Boolean activo,
                 Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.usuario = usuario;
    this.password = password;
    this.nombre = nombre;
    this.direccion = direccion;
    this.telefono = telefono;
    this.tipo = tipo;
    this.activo = activo;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUsuario() { return usuario; }
  public void setUsuario(String usuario) { this.usuario = usuario; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getDireccion() { return direccion; }
  public void setDireccion(String direccion) { this.direccion = direccion; }
  public String getTelefono() { return telefono; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public TipoUsuario getTipo() { return tipo; }
  public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }
  public Boolean getActivo() { return activo; }
  public void setActivo(Boolean activo) { this.activo = activo; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
