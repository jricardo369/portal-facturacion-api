package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.FacturaEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.entity.TicketFacturaEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.mapper.FacturaMapper;
import com.portalfacturacion.domain.model.TicketFactura;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.FacturaRepositoryPort;
import com.portalfacturacion.domain.model.Factura;
import com.portalfacturacion.domain.model.Ticket;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
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
  private final TicketFacturaJpaRepository ticketFacturaJpaRepository;

  public FacturaRepositoryAdapter(FacturaJpaRepository jpa, TicketFacturaJpaRepository ticketFacturaJpaRepository) {
    this.jpa = jpa;
    this.ticketFacturaJpaRepository = ticketFacturaJpaRepository;
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
    List<TicketFacturaEntity> tickets =
        ticketFacturaJpaRepository.findByNumeroTicket(noTicket);
    if (tickets.isEmpty()) {
      return Optional.empty();
    }
    List<Long> ids = tickets.stream()
        .map(TicketFacturaEntity::getIdFactura).distinct().toList();
    List<FacturaEntity> facturas = jpa.findAllById(ids).stream()
        .filter(e -> "Cargada".equals(e.getEstatus())).toList();
    if (facturas.isEmpty()) {
      return Optional.empty();
    }
    FacturaEntity elegida = facturas.stream()
        .max(java.util.Comparator.comparing(FacturaEntity::getCreatedAt,
            java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
        .orElse(facturas.get(0));
    return Optional.of(FacturaMapper.toDomain(elegida));
  }

  @Override
  public boolean existsByNoTicket(String noTicket) {
    List<TicketFacturaEntity> tickets = ticketFacturaJpaRepository.findByNumeroTicket(noTicket);
    if (tickets.isEmpty()) {
      return false;
    }
    List<Long> ids = tickets.stream().map(TicketFacturaEntity::getIdFactura).distinct().toList();
    return jpa.existsByIdFacturaInAndEstatus(ids, "Cargada");
  }

  @Override
public PagedResult<Factura> findConFiltros(Instant desde, Instant hasta, String noTicket, String cliente, String rfc,
                                              String estatus, String uuid, Instant createdAt, java.math.BigDecimal total,
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
        jakarta.persistence.criteria.Subquery<Long> sq = query.subquery(Long.class);
        jakarta.persistence.criteria.Root<TicketFacturaEntity> tf = sq.from(TicketFacturaEntity.class);
        sq.select(tf.get("idFactura"));
        sq.where(cb.equal(tf.get("numeroTicket"), noTicket.trim()));
        predicates.add(root.get("idFactura").in(sq));
      }
      if (cliente != null && !cliente.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("cliente")), "%" + cliente.trim().toLowerCase() + "%"));
      }
      if (rfc != null && !rfc.isBlank()) {
        predicates.add(cb.equal(cb.upper(root.get("rfc")), rfc.trim().toUpperCase()));
      }
      if (estatus != null && !estatus.isBlank()) {
        predicates.add(cb.equal(cb.upper(root.get("estatus")), estatus.trim().toUpperCase()));
      }
      if (uuid != null && !uuid.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("uuid")), "%" + uuid.trim().toLowerCase() + "%"));
      }
      if (createdAt != null) {
        predicates.add(cb.equal(root.get("createdAt"), createdAt));
      }
      if (total != null) {
        predicates.add(cb.equal(root.get("total"), total));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    }, pageable);
    List<Factura> content = result.getContent().stream().map(e -> {
      Factura f = FacturaMapper.toDomain(e);
       f.setTickets(ticketFacturaJpaRepository.findByIdFactura(e.getIdFactura())
           .stream().map(te -> {
             TicketFactura tf = new TicketFactura();
             tf.setId(te.getId());
             tf.setNumeroTicket(te.getNumeroTicket());
             tf.setFechaEmision(te.getFechaEmision());
             tf.setHora(te.getHora());
             tf.setSubtotal(te.getSubtotal());
             tf.setIva(te.getIva());
             tf.setIsr(te.getIsr());
             tf.setCedular(te.getCedular());
             tf.setTotal(te.getTotal());
             return tf;
           }).toList());
      return f;
    }).toList();
    return new PagedResult<>(content, result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  @Override
  public void guardarTicketsFactura(Long idFactura, List<Ticket> tickets) {
    if (idFactura == null || tickets == null || tickets.isEmpty()) {
      return;
    }
    for (Ticket ticket : tickets) {
      TicketFacturaEntity entity = new TicketFacturaEntity();
      entity.setIdFactura(idFactura);
      entity.setNumeroTicket(ticket.getNumeroTicket());
      entity.setFechaEmision(ticket.getFechaEmision());
      entity.setSubtotal(ticket.getSubtotal() != null ? ticket.getSubtotal() : BigDecimal.ZERO);
      entity.setIva(ticket.getIva() != null ? ticket.getIva() : BigDecimal.ZERO);
      entity.setIsr(ticket.getIsr() != null ? ticket.getIsr() : BigDecimal.ZERO);
      entity.setCedular(ticket.getCedular() != null ? ticket.getCedular() : BigDecimal.ZERO);
      entity.setTotal(ticket.getTotal() != null ? ticket.getTotal() : BigDecimal.ZERO);
      ticketFacturaJpaRepository.save(entity);
    }
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
