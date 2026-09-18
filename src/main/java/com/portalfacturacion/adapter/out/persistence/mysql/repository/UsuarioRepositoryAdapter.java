package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.UsuarioEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.mapper.UsuarioMapper;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.UsuarioRepositoryPort;
import com.portalfacturacion.domain.model.TipoUsuario;
import com.portalfacturacion.domain.model.Usuario;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

  private final UsuarioJpaRepository jpa;

  public UsuarioRepositoryAdapter(UsuarioJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Usuario save(Usuario usuario) {
    UsuarioEntity entity = UsuarioMapper.toEntity(usuario);
    UsuarioEntity saved = jpa.save(entity);
    return UsuarioMapper.toDomain(saved);
  }

  @Override
  public Optional<Usuario> findById(Long id) {
    return jpa.findById(id).map(UsuarioMapper::toDomain);
  }

  @Override
  public Optional<Usuario> findByUsuario(String usuario) {
    return jpa.findByUsuario(usuario).map(UsuarioMapper::toDomain);
  }

  @Override
  public boolean existsByUsuario(String usuario) {
    return jpa.existsByUsuario(usuario);
  }

  @Override
  public PagedResult<Usuario> findActivos(String usuario, String nombre, TipoUsuario tipo,
                                          int page, int size, String sort) {
    Sort sortObj = parseSort(sort);
    PageRequest pageable = PageRequest.of(page, size, sortObj);
    Page<UsuarioEntity> result = jpa.findAll((root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.isTrue(root.get("activo")));
      if (usuario != null && !usuario.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("usuario")), "%" + usuario.toLowerCase() + "%"));
      }
      if (nombre != null && !nombre.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
      }
      if (tipo != null) {
        predicates.add(cb.equal(root.get("tipo"), UsuarioEntity.TipoEntity.valueOf(tipo.name())));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    }, pageable);
    List<Usuario> content = result.getContent().stream().map(UsuarioMapper::toDomain).toList();
    return new PagedResult<>(content, result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  private Sort parseSort(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "id");
    }
    try {
      String[] parts = sort.split(",");
      String prop = parts[0].trim();
      Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
          ? Sort.Direction.ASC : Sort.Direction.DESC;
      return Sort.by(dir, prop);
    } catch (Exception ex) {
      return Sort.by(Sort.Direction.DESC, "id");
    }
  }
}
