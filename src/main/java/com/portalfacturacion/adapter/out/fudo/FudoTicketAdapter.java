package com.portalfacturacion.adapter.out.fudo;

import com.fasterxml.jackson.databind.JsonNode;
import com.portalfacturacion.application.port.out.fudo.FudoTicketPort;
import com.portalfacturacion.configuration.FudoProperties;
import com.portalfacturacion.domain.exception.FudoNoDisponibleException;
import com.portalfacturacion.domain.model.Ticket;
import com.portalfacturacion.domain.model.TicketItem;
import com.portalfacturacion.domain.model.TicketPago;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class FudoTicketAdapter implements FudoTicketPort {

  private final FudoProperties properties;
  private final FudoAuthClient authClient;
  private final RestClient restClient;

  public FudoTicketAdapter(FudoProperties properties, FudoAuthClient authClient, RestClient.Builder builder) {
    this.properties = properties;
    this.authClient = authClient;
    this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
  }

  @Override
  public Optional<Ticket> buscarPorFolio(String numeroTicket) {
    String folio = numeroTicket == null ? "" : numeroTicket.trim();
    if (folio.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(obtenerVenta(folio, false));
    } catch (HttpClientErrorException.NotFound ex) {
      return Optional.empty();
    }
  }

  private Ticket obtenerVenta(String folio, boolean reintentado) {
    String token = authClient.obtenerToken();
    try {
      JsonNode root = restClient.get()
          .uri(uriBuilder -> uriBuilder.path("/sales/{id}")
              .queryParam("include", properties.getSaleInclude())
              .build(folio))
          .header("Authorization", "Bearer " + token)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(JsonNode.class);
      return mapearTicket(folio, root);
    } catch (HttpClientErrorException.Unauthorized ex) {
      if (!reintentado) {
        authClient.invalidarToken();
        return obtenerVenta(folio, true);
      }
      throw new FudoNoDisponibleException("FuDo rechazo la autenticacion", ex);
    } catch (HttpClientErrorException.NotFound ex) {
      throw ex;
    } catch (HttpClientErrorException ex) {
      throw new FudoNoDisponibleException("Error de FuDo: " + ex.getStatusCode(), ex);
    } catch (FudoNoDisponibleException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new FudoNoDisponibleException("No fue posible consultar el ticket en FuDo", ex);
    }
  }

  Ticket mapearTicket(String folio, JsonNode root) {
    if (root == null || !root.has("data") || root.get("data").isNull()) {
      return null;
    }
    JsonNode data = root.get("data");
    JsonNode attrs = data.path("attributes");

    String estado = texto(attrs, "saleState", "sale_state", "state");
    String tipoVenta = texto(attrs, "saleType", "sale_type");
    BigDecimal total = numero(attrs, "total");
    Instant creado = instante(attrs, "createdAt", "created_at");
    Instant cerrado = instante(attrs, "closedAt", "closed_at");
    String cliente = texto(attrs, "customerName", "customer_name");
    if (cliente == null || cliente.isBlank()) {
      cliente = texto(attrs.path("anonymousCustomer"), "name");
    }

    Map<String, JsonNode> incluidos = indexarIncluidos(root.path("included"));
    String nombreCliente = resolverNombreCliente(data, incluidos);
    if (nombreCliente != null && !nombreCliente.isBlank()) {
      cliente = nombreCliente;
    }
    String mesa = resolverMesa(data, incluidos);

    List<TicketItem> items = resolverItems(data, incluidos);
    List<TicketPago> pagos = resolverPagos(data, incluidos);

    BigDecimal subtotal = items.stream()
        .map(TicketItem::getImporte)
        .filter(v -> v != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (subtotal.compareTo(BigDecimal.ZERO) == 0 && total != null) {
      subtotal = total;
    }

    boolean facturable = HttpStatus.OK.value() > 0 && "CLOSED".equalsIgnoreCase(estado);

    return new Ticket(folio, total, subtotal, null, estado, tipoVenta,
        creado, cerrado, cliente, mesa, null, facturable, items, pagos);
  }

  private Map<String, JsonNode> indexarIncluidos(JsonNode included) {
    Map<String, JsonNode> mapa = new HashMap<>();
    if (included != null && included.isArray()) {
      for (JsonNode n : included) {
        String type = n.path("type").asText("");
        String id = n.path("id").asText("");
        if (!type.isBlank() && !id.isBlank()) {
          mapa.put(type + ":" + id, n);
        }
      }
    }
    return mapa;
  }

  private List<TicketItem> resolverItems(JsonNode data, Map<String, JsonNode> incluidos) {
    List<TicketItem> items = new ArrayList<>();
    for (JsonNode ref : relaciones(data, "items")) {
      String id = ref.path("id").asText(null);
      String type = ref.path("type").asText("Item");
      JsonNode nodo = incluidos.get(type + ":" + id);
      if (nodo == null) {
        continue;
      }
      JsonNode a = nodo.path("attributes");
      BigDecimal cantidad = numero(a, "quantity", "qty", "count");
      if (cantidad == null) {
        cantidad = BigDecimal.ONE;
      }
      BigDecimal precio = numero(a, "price", "unitPrice", "unit_price");
      BigDecimal importe = numero(a, "total", "amount", "importe");
      if (importe == null && precio != null) {
        importe = precio.multiply(cantidad);
      }
      String descripcion = texto(a, "comment", "detail", "name");
      String productoId = idRel(nodo, "product");
      if (productoId != null) {
        JsonNode prod = incluidos.get("Product:" + productoId);
        if (prod == null) {
          prod = buscarIncluidoPorId(incluidos, productoId);
        }
        if (prod != null) {
          String nombreProd = texto(prod.path("attributes"), "name", "nombre");
          if (nombreProd != null && !nombreProd.isBlank()) {
            descripcion = nombreProd;
          }
        }
      }
      if (descripcion == null || descripcion.isBlank()) {
        descripcion = "Item " + id;
      }
      items.add(new TicketItem(id, descripcion, cantidad, precio, importe));
    }
    return items;
  }

  private List<TicketPago> resolverPagos(JsonNode data, Map<String, JsonNode> incluidos) {
    List<TicketPago> pagos = new ArrayList<>();
    for (JsonNode ref : relaciones(data, "payments")) {
      String id = ref.path("id").asText(null);
      String type = ref.path("type").asText("Payment");
      JsonNode nodo = incluidos.get(type + ":" + id);
      if (nodo == null) {
        continue;
      }
      JsonNode a = nodo.path("attributes");
      BigDecimal monto = numero(a, "amount", "total", "monto");
      String metodo = null;
      String metodoId = idRel(nodo, "paymentMethod");
      if (metodoId != null) {
        JsonNode pm = incluidos.get("PaymentMethod:" + metodoId);
        if (pm == null) {
          pm = buscarIncluidoPorId(incluidos, metodoId);
        }
        if (pm != null) {
          metodo = texto(pm.path("attributes"), "name", "code", "kind");
        }
      }
      if (metodo == null || metodo.isBlank()) {
        metodo = texto(a, "paymentMethod", "method", "medio");
      }
      pagos.add(new TicketPago(id, metodo, monto));
    }
    return pagos;
  }

  private String resolverNombreCliente(JsonNode data, Map<String, JsonNode> incluidos) {
    String id = idRel(data, "customer");
    if (id == null) {
      return null;
    }
    JsonNode c = incluidos.get("Customer:" + id);
    if (c == null) {
      c = buscarIncluidoPorId(incluidos, id);
    }
    return c == null ? null : texto(c.path("attributes"), "name", "nombre");
  }

  private String resolverMesa(JsonNode data, Map<String, JsonNode> incluidos) {
    String id = idRel(data, "table");
    if (id == null) {
      return null;
    }
    JsonNode t = incluidos.get("Table:" + id);
    if (t == null) {
      t = buscarIncluidoPorId(incluidos, id);
    }
    return t == null ? null : texto(t.path("attributes"), "name", "number", "nombre");
  }

  private List<JsonNode> relaciones(JsonNode data, String nombre) {
    List<JsonNode> refs = new ArrayList<>();
    JsonNode rel = data.path("relationships").path(nombre).path("data");
    if (rel.isArray()) {
      rel.forEach(refs::add);
    } else if (rel.isObject()) {
      refs.add(rel);
    }
    return refs;
  }

  private String idRel(JsonNode nodo, String nombre) {
    JsonNode d = nodo.path("relationships").path(nombre).path("data");
    if (d.isObject() && d.hasNonNull("id")) {
      return d.get("id").asText();
    }
    return null;
  }

  private JsonNode buscarIncluidoPorId(Map<String, JsonNode> incluidos, String id) {
    for (Map.Entry<String, JsonNode> e : incluidos.entrySet()) {
      if (e.getKey().endsWith(":" + id)) {
        return e.getValue();
      }
    }
    return null;
  }

  private String texto(JsonNode nodo, String... claves) {
    if (nodo == null || nodo.isMissingNode()) {
      return null;
    }
    for (String c : claves) {
      JsonNode v = nodo.get(c);
      if (v != null && !v.isNull() && !v.asText("").isBlank()) {
        return v.asText();
      }
    }
    return null;
  }

  private BigDecimal numero(JsonNode nodo, String... claves) {
    if (nodo == null || nodo.isMissingNode()) {
      return null;
    }
    for (String c : claves) {
      JsonNode v = nodo.get(c);
      if (v != null && v.isNumber()) {
        return new BigDecimal(v.asText());
      }
      if (v != null && !v.isNull()) {
        try {
          return new BigDecimal(v.asText().trim());
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return null;
  }

  private Instant instante(JsonNode nodo, String... claves) {
    String s = texto(nodo, claves);
    if (s == null || s.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(s);
    } catch (Exception ex) {
      return null;
    }
  }
}
