package com.mcp.mcpgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MCPGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCPGatewayApplication.class, args);
    }
} 