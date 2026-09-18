package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long>, JpaSpecificationExecutor<UsuarioEntity> {
  Optional<UsuarioEntity> findByUsuario(String usuario);
  boolean existsByUsuario(String usuario);

  default Page<UsuarioEntity> findActivos(Specification<UsuarioEntity> spec, Pageable pageable) {
    return findAll(spec, pageable);
  }
}
