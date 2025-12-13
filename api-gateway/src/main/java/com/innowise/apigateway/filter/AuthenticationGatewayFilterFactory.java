package com.innowise.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public AuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header");
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                if (!jwtUtil.isTokenValid(authHeader, false)) {
                    return onError(exchange, "Invalid Token");
                }

                String userId = String.valueOf(jwtUtil.extractUserId(authHeader, false));
                String role = jwtUtil.extractRole(authHeader, false);

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r.headers(headers -> {
                            headers.add("X-User-Id", userId);
                            headers.add("X-User-Role", role);
                        }))
                        .build();

                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                return onError(exchange, "Unauthorized access");
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}