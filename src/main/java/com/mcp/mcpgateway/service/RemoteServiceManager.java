package com.mcp.mcpgateway.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.mcp.mcpgateway.config.RemoteServicesConfig;
import com.mcp.mcpgateway.config.RemoteServicesConfig.RemoteServiceConfig;
import com.mcp.mcpgateway.model.Tool;
import com.mcp.mcpgateway.model.ToolParameter;
import com.mcp.mcpgateway.model.ToolReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RemoteServiceManager {

    private final RemoteServicesConfig config;
    private final RestTemplate restTemplate;
    private final Map<String, List<Tool>> remoteToolsCache = new HashMap<>();
    private ScheduledExecutorService scheduler;

    public RemoteServiceManager(RemoteServicesConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void initialize() {
        if (!config.isEnabled()) {
            log.info("Remote services are disabled, skipping initialization");
            return;
        }

        log.info("Initializing remote service manager with {} services",
                config.getServices() != null ? config.getServices().size() : 0);

        refreshRemoteTools();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                this::refreshRemoteTools,
                config.getRefreshIntervalMs(),
                config.getRefreshIntervalMs(),
                TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void cleanup() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public synchronized void refreshRemoteTools() {
        log.debug("Refreshing remote tools");

        if (!config.isEnabled() || config.getServices() == null) {
            return;
        }

        for (RemoteServiceConfig serviceConfig : config.getServices()) {
            if (!serviceConfig.isEnabled()) {
                log.debug("Skipping disabled service: {}", serviceConfig.getId());
                continue;
            }

            try {
                List<Tool> tools = fetchToolsFromService(serviceConfig);
                remoteToolsCache.put(serviceConfig.getId(), tools);
                log.info("Fetched {} tools from service {}", tools.size(), serviceConfig.getId());
                
                // Log details of registered tools
                for (Tool tool : tools) {
                    log.debug("Registered tool: ID={}, Name={}", serviceConfig.getId(), tool.getName());
                }
            } catch (Exception e) {
                log.error("Failed to fetch tools from service {}: {}",
                        serviceConfig.getId(), e.getMessage(), e);
            }
        }
    }

    private List<Tool> fetchToolsFromService(RemoteServiceConfig serviceConfig) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(serviceConfig.getUrl())
                    .path(serviceConfig.getToolsEndpoint())
                    .build()
                    .toUriString();

            log.debug("Fetching tools from URL: {}", url);

            ResponseEntity<List<Tool>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Tool>>() {
                    });

            List<Tool> tools = response.getBody();
            if (tools == null) {
                return Collections.emptyList();
            }

            List<Tool> processedTools = new ArrayList<>();
            for (Tool tool : tools) {
                Tool prefixedTool = createPrefixedCopy(tool, serviceConfig);
                processedTools.add(prefixedTool);
            }

            return processedTools;
        } catch (RestClientException e) {
            log.error("Error fetching tools from service {}: {}",
                    serviceConfig.getId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Tool createPrefixedCopy(Tool tool, RemoteServiceConfig serviceConfig) {
        Tool prefixedTool = new Tool();
        prefixedTool.setName(serviceConfig.getId() + "." + tool.getName());
        prefixedTool.setDescription(tool.getDescription());
        prefixedTool.setParameters(tool.getParameters());
        prefixedTool.setReturns(tool.getReturns());

        // Create metadata map if needed
        Map<String, Object> metadata = new HashMap<>();
        if (tool.getMetadata() != null) {
            metadata.putAll(tool.getMetadata());
        }

        // Add service metadata
        metadata.put("serviceId", serviceConfig.getId());
        metadata.put("originalToolName", tool.getName());
        prefixedTool.setMetadata(metadata);

        return prefixedTool;
    }

    public List<Tool> getAllRemoteTools() {
        List<Tool> allTools = new ArrayList<>();

        // Collect tools from each enabled service
        for (String serviceId : remoteToolsCache.keySet()) {
            List<Tool> serviceTools = remoteToolsCache.get(serviceId);
            allTools.addAll(serviceTools);
        }

        return allTools;
    }

    public Object executeRemoteTool(String serviceId, String toolName, Map<String, Object> parameters) {
        // Find the service configuration
        RemoteServiceConfig serviceConfig = findServiceById(serviceId);
        if (serviceConfig == null) {
            throw new IllegalArgumentException("Service not found: " + serviceId);
        }
        
        // Determine which name format to use based on the endpoint pattern
        String nameToUse = toolName;
        
        // If endpoint contains {tool} (placeholder format), use the original tool name without prefix
        if (serviceConfig.getExecutionEndpoint().contains("{tool}")) {
            // Extract original tool name without service prefix if needed
            if (toolName.startsWith(serviceId + ".")) {
                nameToUse = toolName.substring((serviceId + ".").length());
                log.debug("Using original tool name without prefix: {}", nameToUse);
            }
        }
        
        // Build the URL for execution - Replace {tool} placeholder with appropriate tool name
        String executionEndpoint = serviceConfig.getExecutionEndpoint().replace("{tool}", nameToUse);
        
        String url = UriComponentsBuilder
                .fromUriString(serviceConfig.getUrl())
                .path(executionEndpoint)
                .build()
                .toUriString();
        
        log.debug("Executing remote tool at URL: {}", url);
        log.debug("Using parameters: {}", parameters);
        
        // Create headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Accept", "application/json");
        
        try {
            // Execute the request
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(parameters, httpHeaders);
            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error executing remote service: {}", e.getMessage(), e);
            
            // In case of error, create a generic error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return errorResponse;
        }
    }

    private RemoteServiceConfig findServiceById(String serviceId) {
        if (config.getServices() == null) {
            return null;
        }

        for (RemoteServiceConfig serviceConfig : config.getServices()) {
            if (serviceConfig.getId().equals(serviceId)) {
                return serviceConfig;
            }
        }

        return null;
    }
} 