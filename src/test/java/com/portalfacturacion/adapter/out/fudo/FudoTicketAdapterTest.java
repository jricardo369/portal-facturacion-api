package com.portalfacturacion.adapter.out.fudo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portalfacturacion.configuration.FudoProperties;
import com.portalfacturacion.domain.model.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FudoTicketAdapterTest {

  @Test
  void mapearTicket_ventaCerrada_conItemsYPagos() throws Exception {
    String json = """
        {
          "data": {
            "id": "123",
            "type": "Sale",
            "attributes": {
              "total": 1350.50,
              "saleState": "CLOSED",
              "saleType": "EAT-IN",
              "createdAt": "2026-09-01T10:00:00.000Z",
              "closedAt": "2026-09-01T11:00:00.000Z",
              "customerName": "Paula"
            },
            "relationships": {
              "items": { "data": [{ "id": "10", "type": "Item" }] },
              "payments": { "data": [{ "id": "20", "type": "Payment" }] },
              "table": { "data": { "id": "5", "type": "Table" } }
            }
          },
          "included": [
            { "id": "10", "type": "Item",
              "attributes": { "quantity": 2, "price": 500.00 },
              "relationships": { "product": { "data": { "id": "7", "type": "Product" } } } },
            { "id": "7", "type": "Product", "attributes": { "name": "Cafe Latte" } },
            { "id": "20", "type": "Payment",
              "attributes": { "amount": 1350.50 },
              "relationships": { "paymentMethod": { "data": { "id": "3", "type": "PaymentMethod" } } } },
            { "id": "3", "type": "PaymentMethod", "attributes": { "name": "Cash" } },
            { "id": "5", "type": "Table", "attributes": { "name": "Mesa 1" } }
          ]
        }
        """;
    FudoProperties props = new FudoProperties();
    FudoTicketAdapter adapter = new FudoTicketAdapter(props, mock(FudoAuthClient.class), mock(RestClient.Builder.class, org.mockito.Answers.RETURNS_MOCKS));

    Ticket ticket = adapter.mapearTicket("123", new ObjectMapper().readTree(json));

    assertThat(ticket.getNumeroTicket()).isEqualTo("123");
    assertThat(ticket.getTotal()).isEqualByComparingTo("1350.50");
    assertThat(ticket.getEstado()).isEqualTo("CLOSED");
    assertThat(ticket.isFacturable()).isTrue();
    assertThat(ticket.getItems()).hasSize(1);
    assertThat(ticket.getItems().get(0).getDescripcion()).isEqualTo("Cafe Latte");
    assertThat(ticket.getPagos()).hasSize(1);
    assertThat(ticket.getPagos().get(0).getMetodo()).isEqualTo("Cash");
    assertThat(ticket.getMesa()).isEqualTo("Mesa 1");
  }

  @Test
  void mapearTicket_ventaAbierta_noFacturable() throws Exception {
    String json = """
        { "data": { "id": "9", "type": "Sale",
          "attributes": { "total": 100, "saleState": "IN-COURSE" } } }
        """;
    FudoProperties props = new FudoProperties();
    FudoTicketAdapter adapter = new FudoTicketAdapter(props, mock(FudoAuthClient.class), mock(RestClient.Builder.class, org.mockito.Answers.RETURNS_MOCKS));

    Ticket ticket = adapter.mapearTicket("9", new ObjectMapper().readTree(json));

    assertThat(ticket.isFacturable()).isFalse();
  }
}
