package com.bootcamp67.ms_transaction.util;

import org.springframework.web.server.ServerWebExchange;

public class SecurityContextUtil {

  private static final String USERNAME_ATTR = "username";
  private static final String CUSTOMER_ID_ATTR = "customerId";
  private static final String ROLE_ATTR = "role";

  /**
   * Get authenticated username
   */
  public static String getUsername(ServerWebExchange exchange) {
    return (String) exchange.getAttributes().get(USERNAME_ATTR);
  }

  /**
   * Get authenticated customer ID
   */
  public static String getCustomerId(ServerWebExchange exchange) {
    return (String) exchange.getAttributes().get(CUSTOMER_ID_ATTR);
  }

  /**
   * Get authenticated user role
   */
  public static String getRole(ServerWebExchange exchange) {
    return (String) exchange.getAttributes().get(ROLE_ATTR);
  }

  /**
   * Check if user has specific role
   */
  public static boolean hasRole(ServerWebExchange exchange, String role) {
    String userRole = getRole(exchange);
    return userRole != null && userRole.equalsIgnoreCase(role);
  }

  /**
   * Check if user is ADMIN
   */
  public static boolean isAdmin(ServerWebExchange exchange) {
    return hasRole(exchange, "ADMIN");
  }

  /**
   * Check if user is USER
   */
  public static boolean isUser(ServerWebExchange exchange) {
    return hasRole(exchange, "USER");
  }

  /**
   * Check if transaction belongs to authenticated customer or user is ADMIN
   */
  public static boolean canAccessCustomerData(ServerWebExchange exchange, String targetCustomerId) {
    if (isAdmin(exchange)) {
      return true;
    }

    String authenticatedCustomerId = getCustomerId(exchange);
    return authenticatedCustomerId != null && authenticatedCustomerId.equals(targetCustomerId);
  }
}
