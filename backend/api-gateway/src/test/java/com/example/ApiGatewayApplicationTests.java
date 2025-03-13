package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
	    "spring.cloud.discovery.enabled=false",
	    "spring.cloud.gateway.discovery.locator.enabled=false"
	})
	@ActiveProfiles("test")
	class ApiGatewayApplicationTests {
	    @Test
	    void contextLoads() {
	    }
	}
