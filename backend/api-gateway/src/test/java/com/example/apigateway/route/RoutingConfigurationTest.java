package com.example.apigateway.route;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class RoutingConfigurationTest {

        @Autowired
        private RouteLocator routeLocator;

        @Test
        void shouldConfigureUserServiceRoute() {
                StepVerifier.create(routeLocator.getRoutes()
                                .filter(route -> route.getId().equals("user-service"))
                                .collectList())
                                .assertNext(routes -> {
                                        assertThat(routes).hasSize(1);
                                        Route route = routes.get(0);

                                        assertThat(route.getPredicate().toString())
                                                        .contains("/api/users/**");

                                        assertThat(route.getUri().toString())
                                                        .contains("http://localhost:9091");

                                        List<GatewayFilter> filters = route.getFilters();

                                        assertThat(filters.stream()
                                                        .map(GatewayFilter::toString)
                                                        .anyMatch(s -> s.contains("CircuitBreaker")
                                                                        && s.contains("fallbackUri")))
                                                        .isTrue();
                                })
                                .verifyComplete();
        }

        @Test
        void shouldApplyAuthenticationFilterToUserServiceRoute() {
                StepVerifier.create(routeLocator.getRoutes()
                                .filter(route -> route.getId().equals("user-service"))
                                .collectList())
                                .assertNext(routes -> {
                                        Route route = routes.get(0);

                                        List<String> filterStrings = route.getFilters().stream()
                                                        .map(Object::toString)
                                                        .collect(java.util.stream.Collectors.toList());

                                        assertThat(filterStrings.stream().anyMatch(s -> s.contains("Authentication")))
                                                        .isTrue();

                                        assertThat(filterStrings.stream().anyMatch(s -> s.contains("Validation")))
                                                        .isTrue();
                                })
                                .verifyComplete();
        }
}