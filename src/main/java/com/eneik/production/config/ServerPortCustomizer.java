package com.eneik.production.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Customizer to ensure the embedded server binds to the expected internal container port.
 * When SERVER_PORT is set in the host environment for Docker port mapping (e.g. 18080:8080),
 * Spring Boot's default relaxed binding might map SERVER_PORT to server.port, causing Tomcat
 * to listen on port 18080 inside the container rather than container port 8080.
 * This customizer explicitly binds the container web server port to 8080 (or CONTAINER_PORT/PORT/APP_PORT).
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Value("${app.server.port:${PORT:${CONTAINER_PORT:8080}}}")
    private int port;

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        factory.setPort(port);
    }

    public int getPort() {
        return port;
    }
}
