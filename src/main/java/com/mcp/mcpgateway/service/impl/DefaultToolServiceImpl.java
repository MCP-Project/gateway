package com.mcp.mcpgateway.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.mcp.mcpgateway.model.Tool;
import com.mcp.mcpgateway.model.ToolExecutionRequest;
import com.mcp.mcpgateway.service.RemoteServiceManager;
import com.mcp.mcpgateway.service.ToolService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class DefaultToolServiceImpl implements ToolService {

    private final MockToolServiceImpl mockToolService;
    private final RemoteServiceManager remoteServiceManager;

    private final Map<String, Tool> allTools = new ConcurrentHashMap<>();

    @Value("${mcp.include-mock-tools:true}")
    private boolean includeMockTools;

    @PostConstruct
    public void initialize() {
        refreshToolRegistry();
    }


    public void refreshToolRegistry() {
        allTools.clear();

        if (includeMockTools) {
            log.info("Including mock tools in the registry");
            for (Tool tool : mockToolService.getAllTools()) {
                allTools.put(tool.getName(), tool);
            }
        }

        List<Tool> remoteTools = remoteServiceManager.getAllRemoteTools();
        log.info("Adding {} remote tools to the registry", remoteTools.size());

        for (Tool tool : remoteTools) {
            allTools.put(tool.getName(), tool);
        }

        log.info("Tool registry refreshed. Total tools: {}", allTools.size());
    }

    @Override
    public List<Tool> getAllTools() {
        return new ArrayList<>(allTools.values());
    }

    @Override
    public Optional<Tool> getToolByName(String name) {
        return Optional.ofNullable(allTools.get(name));
    }

    @Override
    public Optional<Object> executeTool(ToolExecutionRequest request) {
        String toolName = request.getToolName();
        Map<String, Object> params = request.getParameters();

        log.info("Executing tool: {} with parameters: {}", toolName, params);

        Tool tool = allTools.get(toolName);
        if (tool == null) {
            log.error("Tool not found: {}", toolName);
            return Optional.empty();
        }

        if (tool.getMetadata() != null && tool.getMetadata().containsKey("serviceId")) {
            String serviceId = (String) tool.getMetadata().get("serviceId");
            String originalToolName = (String) tool.getMetadata().get("originalToolName");

            log.info("Executing remote tool '{}' on service '{}'", originalToolName, serviceId);

            try {
                Object result = remoteServiceManager.executeRemoteTool(serviceId, originalToolName, params);
                return Optional.ofNullable(result);
            } catch (Exception e) {
                log.error("Error executing remote tool", e);
                return Optional.empty();
            }
        }

        return mockToolService.executeTool(request);
    }
} 