package com.mcp.mcpgateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;


@Data
@Configuration
@ConfigurationProperties(prefix = "mcp.remote-services")
public class RemoteServicesConfig {

    private boolean enabled = false;

    private List<RemoteServiceConfig> services;

    private long refreshIntervalMs = 300000;


    @Data
    public static class RemoteServiceConfig {

        private String id;

        private String url;

        private String toolsEndpoint;

        private String executionEndpoint;

        private boolean enabled = true;
    }
} 