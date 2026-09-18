package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.FacturaEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.mapper.FacturaMapper;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.FacturaRepositoryPort;
import com.portalfacturacion.domain.model.Factura;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class FacturaRepositoryAdapter implements FacturaRepositoryPort {

  private final FacturaJpaRepository jpa;

  public FacturaRepositoryAdapter(FacturaJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Factura save(Factura factura) {
    FacturaEntity entity = FacturaMapper.toEntity(factura);
    FacturaEntity saved = jpa.save(entity);
    return FacturaMapper.toDomain(saved);
  }

  @Override
  public Optional<Factura> findById(Long idFactura) {
    return jpa.findById(idFactura).map(FacturaMapper::toDomain);
  }

  @Override
  public Optional<Factura> findByUuid(String uuid) {
    return jpa.findByUuid(uuid).map(FacturaMapper::toDomain);
  }

  @Override
  public Optional<Factura> findBySerieAndFolio(String serie, String folio) {
    return jpa.findBySerieAndFolio(serie, folio).map(FacturaMapper::toDomain);
  }

  @Override
  public boolean existsBySerieAndFolio(String serie, String folio) {
    return jpa.existsBySerieAndFolio(serie, folio);
  }

  @Override
  public Optional<Factura> findByNoTicket(String noTicket) {
    return jpa.findByNoTicket(noTicket).map(FacturaMapper::toDomain);
  }

  @Override
  public boolean existsByNoTicket(String noTicket) {
    return jpa.existsByNoTicket(noTicket);
  }

  @Override
  public PagedResult<Factura> findConFiltros(Instant desde, Instant hasta, String noTicket,
                                             int page, int size, String sort) {
    PageRequest pageable = PageRequest.of(page, size, parseSort(sort));
    Page<FacturaEntity> result = jpa.findAll((root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (desde != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
      }
      if (hasta != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
      }
      if (noTicket != null && !noTicket.isBlank()) {
        predicates.add(cb.equal(root.get("noTicket"), noTicket.trim()));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    }, pageable);
    List<Factura> content = result.getContent().stream().map(FacturaMapper::toDomain).toList();
    return new PagedResult<>(content, result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  private Sort parseSort(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "idFactura");
    }
    try {
      String[] parts = sort.split(",");
      String prop = parts[0].trim();
      Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
          ? Sort.Direction.ASC : Sort.Direction.DESC;
      return Sort.by(dir, prop);
    } catch (Exception ex) {
      return Sort.by(Sort.Direction.DESC, "idFactura");
    }
  }
}
