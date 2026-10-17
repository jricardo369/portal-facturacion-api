package com.portalfacturacion.adapter.out.facturapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portalfacturacion.configuration.FacturapiProperties;
import com.portalfacturacion.domain.exception.TimbradoFallidoException;
import com.portalfacturacion.domain.model.FacturaTimbrada;
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
}
