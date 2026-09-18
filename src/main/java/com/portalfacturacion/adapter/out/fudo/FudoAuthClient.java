package com.portalfacturacion.adapter.out.fudo;

import com.fasterxml.jackson.databind.JsonNode;
import com.portalfacturacion.configuration.FudoProperties;
import com.portalfacturacion.domain.exception.FudoNoDisponibleException;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FudoAuthClient {

  private final FudoProperties properties;
  private final RestClient restClient;

  private volatile String cachedToken;
  private volatile long expiresAtEpochSec;

  public FudoAuthClient(FudoProperties properties, RestClient.Builder builder) {
    this.properties = properties;
    this.restClient = builder
        .baseUrl(properties.getAuthUrl())
        .build();
  }

  public synchronized String obtenerToken() {
    long now = System.currentTimeMillis() / 1000L;
    if (cachedToken != null && now < expiresAtEpochSec - 60) {
      return cachedToken;
    }
    if (!properties.isConfigured()) {
      throw new FudoNoDisponibleException("FuDo no esta configurado (FUDO_API_KEY / FUDO_API_SECRET)");
    }
    try {
      JsonNode body = restClient.post()
          .uri(properties.getAuthUrl())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .body(Map.of("apiKey", properties.getApiKey(), "apiSecret", properties.getApiSecret()))
          .retrieve()
          .body(JsonNode.class);
      if (body == null || !body.hasNonNull("token")) {
        throw new FudoNoDisponibleException("FuDo no devolvio token de autenticacion");
      }
      cachedToken = body.get("token").asText();
      expiresAtEpochSec = body.has("exp") ? body.get("exp").asLong(now + Duration.ofHours(24).toSeconds()) : now + Duration.ofHours(24).toSeconds();
      return cachedToken;
    } catch (FudoNoDisponibleException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new FudoNoDisponibleException("No fue posible autenticarse con FuDo", ex);
    }
  }

  public synchronized void invalidarToken() {
    cachedToken = null;
    expiresAtEpochSec = 0;
  }
}
