package com.portalfacturacion.adapter.out.facturapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.portalfacturacion.application.port.out.facturapi.FacturapiTimbradoPort;
import com.portalfacturacion.configuration.FacturapiProperties;
import com.portalfacturacion.domain.exception.FacturaNoEncontradaException;
import com.portalfacturacion.domain.exception.FacturapiNoDisponibleException;
import com.portalfacturacion.domain.exception.TimbradoFallidoException;
import com.portalfacturacion.domain.model.FacturaTimbrada;
import com.portalfacturacion.domain.model.ItemSolicitudTimbrado;
import com.portalfacturacion.domain.model.ResultadoCancelacion;
import com.portalfacturacion.domain.model.SolicitudTimbrado;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class FacturapiTimbradoAdapter implements FacturapiTimbradoPort {

  private static final Logger log = LoggerFactory.getLogger(FacturapiTimbradoAdapter.class);
  private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
      new com.fasterxml.jackson.databind.ObjectMapper();

  private final FacturapiProperties properties;
  private final RestClient restClient;

  public FacturapiTimbradoAdapter(FacturapiProperties properties, RestClient.Builder builder) {
    this.properties = properties;
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout((int) properties.getTimeoutMs());
    requestFactory.setReadTimeout((int) properties.getTimeoutMs());
    this.restClient = builder
        .baseUrl(properties.getBaseUrl())
        .requestFactory(requestFactory)
        .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
        .build();
  }

  @Override
  public FacturaTimbrada timbrar(SolicitudTimbrado solicitud) {
    Map<String, Object> cuerpo = construirCuerpo(solicitud);
    JsonNode respuesta = ejecutar(() -> restClient.post()
        .uri("/invoices")
        .contentType(MediaType.APPLICATION_JSON)
        .body(cuerpo)
        .retrieve()
        .body(JsonNode.class));
    if (respuesta == null) {
      throw new TimbradoFallidoException("Facturapi no regreso contenido al timbrar");
    }
    if (respuesta.path("status").asText("").equalsIgnoreCase("pending")
        && respuesta.hasNonNull("id")) {
      respuesta = esperarTimbrado(respuesta.get("id").asText());
    }
    return mapearFactura(respuesta);
  }

  private JsonNode esperarTimbrado(String id) {
    for (int intento = 0; intento < 3; intento++) {
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new TimbradoFallidoException("Timbrado interrumpido mientras Facturapi procesaba el CFDI", ex);
      }
      JsonNode actual = ejecutar(() -> restClient.get()
          .uri("/invoices/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(JsonNode.class));
      if (actual != null && actual.hasNonNull("uuid")) {
        return actual;
      }
    }
    throw new TimbradoFallidoException(
        "Facturapi acepto el CFDI (status 202) pero no lo timbro en el tiempo de espera. "
            + "Verifique en el dashboard de Facturapi con el id " + id + " antes de reintentar");
  }

  Map<String, Object> construirCuerpo(SolicitudTimbrado s) {
    Map<String, Object> cliente = new HashMap<>();
    cliente.put("legal_name", s.getRazonSocial());
    cliente.put("tax_id", s.getRfc());
    cliente.put("tax_system", s.getRegimenFiscal());
    if (s.getCorreo() != null && !s.getCorreo().isBlank()) {
      cliente.put("email", s.getCorreo().trim());
    }
    if (s.getCodigoPostal() != null && !s.getCodigoPostal().isBlank()) {
      cliente.put("address", Map.of("zip", s.getCodigoPostal().trim(), "country", "MEX"));
    }

    List<Map<String, Object>> items = new ArrayList<>();
    if (s.getItems() != null) {
      for (ItemSolicitudTimbrado item : s.getItems()) {
        Map<String, Object> producto = new HashMap<>();
        producto.put("description", item.getDescripcion());
        producto.put("product_key", item.getClave());
        producto.put("price", item.getPrecio());
        producto.put("tax_included", s.isPrecioConImpuestos());
        Map<String, Object> fila = new HashMap<>();
        fila.put("quantity", item.getCantidad());
        fila.put("product", producto);
        items.add(fila);
      }
    }

    Map<String, Object> cuerpo = new HashMap<>();
    cuerpo.put("customer", cliente);
    cuerpo.put("items", items);
    if (s.getUsoCfdi() != null && !s.getUsoCfdi().isBlank()) {
      cuerpo.put("use", s.getUsoCfdi().trim());
    }
    cuerpo.put("payment_form", s.getFormaPago());
    cuerpo.put("payment_method", s.getMetodoPago());
    if (s.isPublicoGeneral()) {
      Map<String, Object> global = new HashMap<>();
      global.put("periodicity", s.getPeriodoPeriodicity());
      global.put("months", s.getPeriodoMeses());
      global.put("year", s.getPeriodoAnio());
      cuerpo.put("global", global);
    }
    return cuerpo;
  }

  @Override
  public ResultadoCancelacion cancelar(String facturapiId, String motivo, String sustitucion) {
    JsonNode respuesta = ejecutar(() -> restClient.delete()
        .uri(uriBuilder -> {
          var builder = uriBuilder.path("/invoices/{id}").queryParam("motive", motivo);
          if (sustitucion != null && !sustitucion.isBlank()) {
            builder.queryParam("substitution", sustitucion);
          }
          return builder.build(facturapiId);
        })
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(JsonNode.class));
    return mapearCancelacion(respuesta);
  }

  @Override
  public Optional<FacturaTimbrada> consultar(String facturapiId) {
    try {
      JsonNode respuesta = ejecutar(() -> restClient.get()
          .uri("/invoices/{id}", facturapiId)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(JsonNode.class));
      return respuesta == null || respuesta.path("id").isMissingNode()
          ? Optional.empty() : Optional.of(mapearFactura(respuesta));
    } catch (FacturaNoEncontradaException ex) {
      return Optional.empty();
    }
  }

  @Override
  public byte[] obtenerXml(String facturapiId) {
    return descargar("/invoices/{id}/xml", facturapiId);
  }

  @Override
  public byte[] obtenerPdf(String facturapiId) {
    return descargar("/invoices/{id}/pdf", facturapiId);
  }

  @Override
  public byte[] obtenerZip(String facturapiId) {
    return descargar("/invoices/{id}/zip", facturapiId);
  }

  private byte[] descargar(String ruta, String facturapiId) {
    byte[] contenido = ejecutar(() -> restClient.get()
        .uri(ruta, facturapiId)
        .accept(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_XML, MediaType.APPLICATION_PDF)
        .retrieve()
        .body(byte[].class));
    if (contenido == null || contenido.length == 0) {
      throw new TimbradoFallidoException("Facturapi no devolvio contenido para " + ruta);
    }
    return contenido;
  }

  FacturaTimbrada mapearFactura(JsonNode root) {
    if (root == null || root.path("id").isMissingNode()) {
      throw new TimbradoFallidoException("Respuesta de Facturapi sin id de factura");
    }
    FacturaTimbrada f = new FacturaTimbrada();
    f.setId(root.path("id").asText(null));
    f.setUuid(texto(root, "uuid"));
    f.setSerie(texto(root, "series"));
    String folio = root.hasNonNull("folio_number") ? root.get("folio_number").asText() : null;
    f.setFolio(folio);
    f.setStatus(texto(root, "status"));
    f.setCancellationStatus(texto(root, "cancellation_status"));
    f.setVerificationUrl(texto(root, "verification_url"));
    if (root.hasNonNull("total")) {
      f.setTotal(new BigDecimal(root.get("total").asText()));
    }
    Desglose desglose = desglosarImpuestos(root);
    f.setSubtotal(desglose.subtotal);
    f.setImpuesto(desglose.impuesto);
    return f;
  }

  private Desglose desglosarImpuestos(JsonNode root) {
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal impuesto = BigDecimal.ZERO;
    for (JsonNode item : root.path("items")) {
      JsonNode producto = item.path("product");
      if (!producto.hasNonNull("price")) {
        continue;
      }
      BigDecimal precio = new BigDecimal(producto.get("price").asText());
      BigDecimal cantidad = item.hasNonNull("quantity")
          ? new BigDecimal(item.get("quantity").asText()) : BigDecimal.ONE;
      BigDecimal tasa = BigDecimal.ZERO;
      for (JsonNode imp : producto.path("taxes")) {
        if (imp.hasNonNull("rate")) {
          tasa = tasa.add(new BigDecimal(imp.get("rate").asText()));
        }
      }
      boolean incluido = !producto.hasNonNull("tax_included") || producto.get("tax_included").asBoolean();
      BigDecimal baseUnitario;
      BigDecimal impuestoUnitario;
      if (tasa.compareTo(BigDecimal.ZERO) == 0) {
        baseUnitario = precio;
        impuestoUnitario = BigDecimal.ZERO;
      } else if (incluido) {
        baseUnitario = precio.divide(BigDecimal.ONE.add(tasa), 6, RoundingMode.HALF_UP);
        impuestoUnitario = precio.subtract(baseUnitario);
      } else {
        baseUnitario = precio;
        impuestoUnitario = precio.multiply(tasa);
      }
      subtotal = subtotal.add(baseUnitario.multiply(cantidad));
      impuesto = impuesto.add(impuestoUnitario.multiply(cantidad));
    }
    Desglose d = new Desglose();
    d.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
    d.impuesto = impuesto.setScale(2, RoundingMode.HALF_UP);
    return d;
  }

  private ResultadoCancelacion mapearCancelacion(JsonNode root) {
    ResultadoCancelacion r = new ResultadoCancelacion();
    if (root != null) {
      r.setId(texto(root, "id"));
      r.setUuid(texto(root, "uuid"));
      r.setStatus(texto(root, "status"));
      r.setCancellationStatus(texto(root, "cancellation_status"));
    }
    return r;
  }

  private <T> T ejecutar(Supplier<T> accion) {
    try {
      return accion.get();
    } catch (HttpClientErrorException.NotFound ex) {
      throw new FacturaNoEncontradaException("Facturapi no encontro el comprobante solicitado");
    } catch (HttpClientErrorException ex) {
      throw new TimbradoFallidoException(mensajeError(ex), ex);
    } catch (HttpServerErrorException | ResourceAccessException ex) {
      log.error("Facturapi no disponible: {}", ex.getMessage(), ex);
      throw new FacturapiNoDisponibleException("Facturapi no esta disponible en este momento", ex);
    }
  }

  private String mensajeError(HttpClientErrorException ex) {
    try {
      JsonNode body = MAPPER.readTree(ex.getResponseBodyAsString());
      String mensaje = body.path("message").asText(null);
      if (mensaje != null && !mensaje.isBlank()) {
        return "Facturapi rechazo la operacion: " + mensaje;
      }
    } catch (Exception ignored) {
    }
    return "Facturapi rechazo la operacion (HTTP " + ex.getStatusCode().value() + ")";
  }

  private String texto(JsonNode nodo, String clave) {
    JsonNode v = nodo.get(clave);
    return v != null && !v.isNull() ? v.asText() : null;
  }

  private static final class Desglose {
    private BigDecimal subtotal;
    private BigDecimal impuesto;
  }
}
