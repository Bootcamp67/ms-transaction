package com.bootcamp67.ms_transaction.config;

import com.bootcamp67.ms_transaction.interceptor.HeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig implements WebFluxConfigurer {

  private final HeaderInterceptor headerInterceptor;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("*")
        .allowedHeaders("*")
        .exposedHeaders("X-Auth-Username", "X-Auth-Customer-Id", "X-Auth-Role");
  }

  @Bean
  public WebFilter addSecurityInterceptor() {
    return headerInterceptor;
  }
}
