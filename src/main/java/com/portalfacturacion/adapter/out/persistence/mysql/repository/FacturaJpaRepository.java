package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.FacturaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long>, JpaSpecificationExecutor<FacturaEntity> {
  Optional<FacturaEntity> findByUuid(String uuid);
  Optional<FacturaEntity> findBySerieAndFolio(String serie, String folio);
  boolean existsBySerieAndFolio(String serie, String folio);
  Optional<FacturaEntity> findByNoTicket(String noTicket);
  boolean existsByNoTicket(String noTicket);
}
