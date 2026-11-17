package com.portalfacturacion.adapter.out.facturapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portalfacturacion.configuration.FacturapiProperties;
import com.portalfacturacion.domain.exception.TimbradoFallidoException;
import com.portalfacturacion.domain.model.FacturaTimbrada;
import com.portalfacturacion.domain.model.ItemSolicitudTimbrado;
import com.portalfacturacion.domain.model.SolicitudTimbrado;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FacturapiTimbradoAdapterTest {

  private FacturapiTimbradoAdapter crearAdapter() {
    return new FacturapiTimbradoAdapter(new FacturapiProperties(),
        mock(RestClient.Builder.class, org.mockito.Answers.RETURNS_MOCKS));
  }

  @Test
  void mapearFactura_timbrada_calculaDesgloseDeIva() throws Exception {
    String json = """
        {
          "id": "58e93bd8e86eb318b019743d",
          "uuid": "39c85a3f-275b-4341-b259-e8971d9f8a94",
          "series": "A",
          "folio_number": 914,
          "status": "valid",
          "cancellation_status": "none",
          "total": 345.60,
          "verification_url": "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?id=abc",
          "items": [
            {
              "quantity": 2,
              "product": {
                "description": "Ukelele",
                "price": 345.60,
                "tax_included": true,
                "taxes": [ { "type": "IVA", "rate": 0.16 } ]
              }
            }
          ]
        }
        """;

    FacturaTimbrada f = crearAdapter().mapearFactura(new ObjectMapper().readTree(json));

    assertThat(f.getId()).isEqualTo("58e93bd8e86eb318b019743d");
    assertThat(f.getUuid()).isEqualTo("39c85a3f-275b-4341-b259-e8971d9f8a94");
    assertThat(f.getSerie()).isEqualTo("A");
    assertThat(f.getFolio()).isEqualTo("914");
    assertThat(f.getStatus()).isEqualTo("valid");
    assertThat(f.getCancellationStatus()).isEqualTo("none");
    assertThat(f.getTotal()).isEqualByComparingTo("345.60");
    assertThat(f.getSubtotal()).isEqualByComparingTo("595.86");
    assertThat(f.getImpuesto()).isEqualByComparingTo("95.34");
  }

  @Test
  void mapearFactura_itemsSinImpuestos_impuestoCero() throws Exception {
    String json = """
        {
          "id": "abc",
          "uuid": "uuid-1",
          "status": "valid",
          "cancellation_status": "none",
          "total": 100.00,
          "items": [
            {
              "quantity": 1,
              "product": {
                "description": "Servicio",
                "price": 100.00,
                "tax_included": false,
                "taxes": []
              }
            }
          ]
        }
        """;

    FacturaTimbrada f = crearAdapter().mapearFactura(new ObjectMapper().readTree(json));

    assertThat(f.getSubtotal()).isEqualByComparingTo("100.00");
    assertThat(f.getImpuesto()).isEqualByComparingTo("0.00");
    assertThat(f.getTotal()).isEqualByComparingTo("100.00");
    assertThat(f.getSerie()).isNull();
    assertThat(f.getFolio()).isNull();
  }

  @Test
  void mapearFactura_precioSinImpuestos_desglosaIvaSobrePrecio() throws Exception {
    String json = """
        {
          "id": "abc",
          "uuid": "uuid-2",
          "status": "valid",
          "total": 116.00,
          "items": [
            {
              "quantity": 1,
              "product": {
                "description": "Producto",
                "price": 100.00,
                "tax_included": false,
                "taxes": [ { "type": "IVA", "rate": 0.16 } ]
              }
            }
          ]
        }
        """;

    FacturaTimbrada f = crearAdapter().mapearFactura(new ObjectMapper().readTree(json));

    assertThat(f.getSubtotal()).isEqualByComparingTo("100.00");
    assertThat(f.getImpuesto()).isEqualByComparingTo("16.00");
  }

  @Test
  void mapearFactura_respuestaSinId_lanzaExcepcion() {
    assertThatThrownBy(() -> crearAdapter().mapearFactura(new ObjectMapper().createObjectNode()))
        .isInstanceOf(TimbradoFallidoException.class);
  }

  @Test
  void mapearFactura_conRetencionIsr_descuentaDelImpuestoNeto() throws Exception {
    String json = """
        {
          "id": "abc",
          "uuid": "uuid-3",
          "status": "valid",
          "total": 1147.50,
          "items": [
            {
              "quantity": 1,
              "product": {
                "description": "Consumo",
                "price": 1147.50,
                "tax_included": true,
                "taxes": [
                  { "type": "IVA", "rate": 0.16, "withholding": false },
                  { "type": "ISR", "rate": 0.0125, "withholding": true }
                ]
              }
            }
          ]
        }
        """;

    FacturaTimbrada f = crearAdapter().mapearFactura(new ObjectMapper().readTree(json));

    assertThat(f.getSubtotal()).isEqualByComparingTo("1000.00");
    assertThat(f.getImpuesto()).isEqualByComparingTo("147.50");
  }

  @Test
  @SuppressWarnings("unchecked")
  void construirCuerpo_conIvaEIsr_incluyeImpuestosTrasladadosYRetenidos() {
    SolicitudTimbrado s = new SolicitudTimbrado();
    s.setRfc("AAAA111111AAA");
    s.setRazonSocial("Empresa X");
    s.setRegimenFiscal("601");
    s.setUsoCfdi("G03");
    s.setFormaPago("01");
    s.setMetodoPago("PUE");
    ItemSolicitudTimbrado item = new ItemSolicitudTimbrado(BigDecimal.ONE, "Consumo",
        new BigDecimal("1147.50"), "90101500");
    item.setIva(new BigDecimal("160.00"));
    item.setIsr(new BigDecimal("12.50"));
    s.setItems(List.of(item));

    Map<String, Object> cuerpo = crearAdapter().construirCuerpo(s);

    List<Map<String, Object>> items = (List<Map<String, Object>>) cuerpo.get("items");
    Map<String, Object> product = (Map<String, Object>) items.get(0).get("product");
    List<Map<String, Object>> taxes = (List<Map<String, Object>>) product.get("taxes");
    assertThat(taxes).hasSize(2);
    assertThat(taxes.get(0))
        .containsEntry("type", "IVA")
        .containsEntry("rate", new BigDecimal("0.16"))
        .containsEntry("withholding", false);
    assertThat(taxes.get(1))
        .containsEntry("type", "ISR")
        .containsEntry("rate", new BigDecimal("0.0125"))
        .containsEntry("withholding", true);
  }

  @Test
  @SuppressWarnings("unchecked")
  void construirCuerpo_publicoGeneral_incluyeNodoGlobal() {
    SolicitudTimbrado s = new SolicitudTimbrado();
    s.setRfc("XAXX010101000");
    s.setRazonSocial("PUBLICO EN GENERAL");
    s.setRegimenFiscal("616");
    s.setUsoCfdi("S01");
    s.setFormaPago("01");
    s.setMetodoPago("PUE");
    s.setPublicoGeneral(true);
    s.setPeriodoPeriodicity("day");
    s.setPeriodoMeses("10");
    s.setPeriodoAnio(2026);
    s.setItems(List.of());

    Map<String, Object> cuerpo = crearAdapter().construirCuerpo(s);

    assertThat(cuerpo).containsKey("global");
    Map<String, Object> global = (Map<String, Object>) cuerpo.get("global");
    assertThat(global)
        .containsEntry("periodicity", "day")
        .containsEntry("months", "10")
        .containsEntry("year", 2026);
    assertThat(cuerpo.get("use")).isEqualTo("S01");
  }

  @Test
  void construirCuerpo_noPublicoGeneral_sinNodoGlobal() {
    SolicitudTimbrado s = new SolicitudTimbrado();
    s.setRfc("AAAA111111AAA");
    s.setRazonSocial("Empresa X");
    s.setRegimenFiscal("601");
    s.setUsoCfdi("G03");
    s.setFormaPago("01");
    s.setMetodoPago("PUE");
    s.setItems(List.of());

    Map<String, Object> cuerpo = crearAdapter().construirCuerpo(s);

    assertThat(cuerpo).doesNotContainKey("global");
  }

  @Test
  @SuppressWarnings("unchecked")
  void construirCuerpo_conUuidSustitucion_incluyeRelacionCfdi04() {
    SolicitudTimbrado s = new SolicitudTimbrado();
    s.setRfc("AAAA111111AAA");
    s.setRazonSocial("Empresa X");
    s.setRegimenFiscal("601");
    s.setUsoCfdi("G03");
    s.setFormaPago("01");
    s.setMetodoPago("PUE");
    s.setUuidSustitucion("uuid-previo");
    s.setItems(List.of());

    Map<String, Object> cuerpo = crearAdapter().construirCuerpo(s);

    List<Map<String, Object>> relaciones =
        (List<Map<String, Object>>) cuerpo.get("related_documents");
    assertThat(relaciones).hasSize(1);
    assertThat(relaciones.get(0))
        .containsEntry("relationship", "04")
        .containsEntry("documents", List.of("uuid-previo"));
  }

  @Test
  void construirCuerpo_sinUuidSustitucion_noIncluyeRelacion() {
    SolicitudTimbrado s = new SolicitudTimbrado();
    s.setRfc("AAAA111111AAA");
    s.setRazonSocial("Empresa X");
    s.setRegimenFiscal("601");
    s.setUsoCfdi("G03");
    s.setFormaPago("01");
    s.setMetodoPago("PUE");
    s.setItems(List.of());

    Map<String, Object> cuerpo = crearAdapter().construirCuerpo(s);

    assertThat(cuerpo).doesNotContainKey("related_documents");
  }
}
