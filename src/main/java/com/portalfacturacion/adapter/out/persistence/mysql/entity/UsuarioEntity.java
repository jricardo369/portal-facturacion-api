package com.portalfacturacion.adapter.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario", nullable = false, unique = true, length = 64)
  private String usuario;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "nombre", length = 150)
  private String nombre;

  @Column(name = "direccion", length = 255)
  private String direccion;

  @Column(name = "telefono", length = 32)
  private String telefono;

  @Column(name = "tipo", length = 32)
  @Enumerated(EnumType.STRING)
  private TipoEntity tipo;

  @Column(name = "activo", nullable = false)
  private Boolean activo = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public enum TipoEntity {
    ADMIN, OPERADOR, CONSULTA
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
  public TipoEntity getTipo() { return tipo; }
  public void setTipo(TipoEntity tipo) { this.tipo = tipo; }
  public Boolean getActivo() { return activo; }
  public void setActivo(Boolean activo) { this.activo = activo; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
