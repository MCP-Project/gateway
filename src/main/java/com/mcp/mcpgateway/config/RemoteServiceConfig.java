package com.mcp.mcpgateway.config;

import lombok.Data;

@Data
public class RemoteServiceConfig {

    private String id;

    private String url;

    private String toolsEndpoint;

    private String executionEndpoint;

    private boolean enabled = true;
} 