package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.ConfiguracionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionJpaRepository extends JpaRepository<ConfiguracionEntity, Long> {
  Optional<ConfiguracionEntity> findByClave(String clave);
}

