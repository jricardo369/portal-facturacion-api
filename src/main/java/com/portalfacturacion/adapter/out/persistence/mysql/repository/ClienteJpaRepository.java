package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.ClienteEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long>, JpaSpecificationExecutor<ClienteEntity> {
  Optional<ClienteEntity> findByRfc(String rfc);
  boolean existsByRfc(String rfc);
}
