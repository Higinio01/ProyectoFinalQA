package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class ApiConfig {

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.version}")
    private String version;

    @Value("${api.context-path}")
    private String contextPath;

    @Value("${server.port}")
    private String serverPort;

    @Value("${integration.external.partner-api.url}")
    private String partnerApiUrl;

    @Value("${integration.external.partner-api.timeout}")
    private int partnerApiTimeout;

    @Value("${integration.external.payment-gateway.url}")
    private String paymentGatewayUrl;

    public String buildUrl(String host, String port, String endpoint) {
        return "http://" + host + ":" + port + contextPath + "/" + version + endpoint;
    }
}