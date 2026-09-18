package com.portalfacturacion.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/api/v1/images/**")
        .addResourceLocations(
            "classpath:/static/images/",
            "file:src/main/webapp/images/",
            "file:src/main/resources/static/images/");
  }
}
