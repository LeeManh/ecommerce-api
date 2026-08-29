package com.ecommerce.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
public class EcommerceBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(EcommerceBackendApplication.class, args);
  }
}
