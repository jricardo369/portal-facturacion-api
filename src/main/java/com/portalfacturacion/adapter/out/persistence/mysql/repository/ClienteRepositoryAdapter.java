package com.portalfacturacion.adapter.out.persistence.mysql.repository;

import com.portalfacturacion.adapter.out.persistence.mysql.entity.ClienteEntity;
import com.portalfacturacion.adapter.out.persistence.mysql.mapper.ClienteMapper;
import com.portalfacturacion.application.port.in.usuario.PagedResult;
import com.portalfacturacion.application.port.out.persistence.ClienteRepositoryPort;
import com.portalfacturacion.domain.model.Cliente;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

  private final ClienteJpaRepository jpa;

  public ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Cliente save(Cliente cliente) {
    ClienteEntity entity = ClienteMapper.toEntity(cliente);
    ClienteEntity saved = jpa.save(entity);
    return ClienteMapper.toDomain(saved);
  }

  @Override
  public Optional<Cliente> findByRfc(String rfc) {
    return jpa.findByRfc(rfc).map(ClienteMapper::toDomain);
  }

  @Override
  public boolean existsByRfc(String rfc) {
    return jpa.existsByRfc(rfc);
  }

  @Override
  public PagedResult<Cliente> findActivos(String rfc, String razonSocial, String codigoPostal,
                                          int page, int size, String sort) {
    PageRequest pageable = PageRequest.of(page, size, parseSort(sort));
    Page<ClienteEntity> result = jpa.findAll((root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.isTrue(root.get("estatus")));
      if (rfc != null && !rfc.isBlank()) {
        predicates.add(cb.like(cb.upper(root.get("rfc")), "%" + rfc.trim().toUpperCase() + "%"));
      }
      if (razonSocial != null && !razonSocial.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("razonSocial")), "%" + razonSocial.toLowerCase() + "%"));
      }
      if (codigoPostal != null && !codigoPostal.isBlank()) {
        predicates.add(cb.equal(root.get("codigoPostal"), codigoPostal.trim()));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    }, pageable);
    List<Cliente> content = result.getContent().stream().map(ClienteMapper::toDomain).toList();
    return new PagedResult<>(content, result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  private Sort parseSort(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "idCliente");
    }
    try {
      String[] parts = sort.split(",");
      String prop = parts[0].trim();
      Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
          ? Sort.Direction.ASC : Sort.Direction.DESC;
      return Sort.by(dir, prop);
    } catch (Exception ex) {
      return Sort.by(Sort.Direction.DESC, "idCliente");
    }
  }
}
