package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.TicketFacturaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketFacturaJpaRepository extends JpaRepository<TicketFacturaEntity, Long> {
  List<TicketFacturaEntity> findByIdFactura(Long idFactura);

  List<TicketFacturaEntity> findByNumeroTicket(String numeroTicket);
}
