package com.bootcamp67.ms_transaction.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HeaderInterceptor implements WebFilter {

  private static final String AUTH_USERNAME_HEADER = "X-Auth-Username";
  private static final String AUTH_CUSTOMER_ID_HEADER = "X-Auth-Customer-Id";
  private static final String AUTH_ROLE_HEADER = "X-Auth-Role";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    // Skip validation for actuator endpoints
    if (path.startsWith("/actuator")) {
      log.debug("Skipping security validation for actuator endpoint: {}", path);
      return chain.filter(exchange);
    }

    // Extract headers
    String username = exchange.getRequest().getHeaders().getFirst(AUTH_USERNAME_HEADER);
    String customerId = exchange.getRequest().getHeaders().getFirst(AUTH_CUSTOMER_ID_HEADER);
    String role = exchange.getRequest().getHeaders().getFirst(AUTH_ROLE_HEADER);

    log.debug("Security validation - Path: {}, Username: {}, CustomerId: {}, Role: {}",
        path, username, customerId, role);

    // Validate required headers
    if (username == null || username.trim().isEmpty()) {
      log.warn("Missing or empty {} header", AUTH_USERNAME_HEADER);
      return unauthorized(exchange, "Missing authentication username");
    }

    if (customerId == null || customerId.trim().isEmpty()) {
      log.warn("Missing or empty {} header", AUTH_CUSTOMER_ID_HEADER);
      return unauthorized(exchange, "Missing customer ID");
    }

    if (role == null || role.trim().isEmpty()) {
      log.warn("Missing or empty {} header", AUTH_ROLE_HEADER);
      return unauthorized(exchange, "Missing role");
    }

    // Store auth data in exchange attributes for controller access
    exchange.getAttributes().put("username", username);
    exchange.getAttributes().put("customerId", customerId);
    exchange.getAttributes().put("role", role);

    log.info("Security validation passed for user: {} with role: {}", username, role);
    return chain.filter(exchange);
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");

    String jsonResponse = String.format("{\"error\": \"%s\"}", message);

    return exchange.getResponse()
        .writeWith(Mono.just(exchange.getResponse()
            .bufferFactory()
            .wrap(jsonResponse.getBytes())));
  }
}
