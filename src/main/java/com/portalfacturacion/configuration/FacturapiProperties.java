package com.portalfacturacion.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.facturapi")
public class FacturapiProperties {

  private String baseUrl = "https://www.facturapi.io/v2";
  private String apiKey;
  private long timeoutMs = 15000;
  private boolean taxIncluded = true;
  private String paymentForm = "01";
  private String paymentMethod = "PUE";
  private String productKey = "01010101";
  private String motivoCancelacion = "02";

  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public String getApiKey() { return apiKey; }
  public void setApiKey(String apiKey) { this.apiKey = apiKey; }
  public long getTimeoutMs() { return timeoutMs; }
  public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
  public boolean isTaxIncluded() { return taxIncluded; }
  public void setTaxIncluded(boolean taxIncluded) { this.taxIncluded = taxIncluded; }
  public String getPaymentForm() { return paymentForm; }
  public void setPaymentForm(String paymentForm) { this.paymentForm = paymentForm; }
  public String getPaymentMethod() { return paymentMethod; }
  public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
  public String getProductKey() { return productKey; }
  public void setProductKey(String productKey) { this.productKey = productKey; }
  public String getMotivoCancelacion() { return motivoCancelacion; }
  public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

  public boolean isConfigured() {
    return apiKey != null && !apiKey.isBlank();
  }
}
