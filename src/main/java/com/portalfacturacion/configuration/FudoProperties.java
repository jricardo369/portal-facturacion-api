package com.portalfacturacion.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.fudo")
public class FudoProperties {

  private String baseUrl = "https://api.fu.do/v1alpha1";
  private String authUrl = "https://auth.fu.do/api";
  private String apiKey;
  private String apiSecret;
  private long timeoutMs = 8000;
  private String saleInclude = "items.product,payments.paymentMethod,customer,table";

  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public String getAuthUrl() { return authUrl; }
  public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }
  public String getApiKey() { return apiKey; }
  public void setApiKey(String apiKey) { this.apiKey = apiKey; }
  public String getApiSecret() { return apiSecret; }
  public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
  public long getTimeoutMs() { return timeoutMs; }
  public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
  public String getSaleInclude() { return saleInclude; }
  public void setSaleInclude(String saleInclude) { this.saleInclude = saleInclude; }

  public boolean isConfigured() {
    return apiKey != null && !apiKey.isBlank() && apiSecret != null && !apiSecret.isBlank();
  }
}
