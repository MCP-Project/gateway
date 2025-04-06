package com.mcp.mcpgateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcp.mcpgateway.model.MCPResponse;
import com.mcp.mcpgateway.model.Tool;
import com.mcp.mcpgateway.model.ToolExecutionRequest;
import com.mcp.mcpgateway.service.ToolService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MCPController {

    private final ToolService toolService;

    @GetMapping("/tools")
    public ResponseEntity<MCPResponse<List<Tool>>> getTools() {
        try {
            List<Tool> tools = toolService.getAllTools();
            return ResponseEntity.ok(MCPResponse.success(tools));
        } catch (Exception e) {
            log.error("Error to find tools", e);
            return ResponseEntity.ok(MCPResponse.error("TOOLS_ERROR", "Error to find tools available"));
        }
    }

    @GetMapping("/tools/{toolName}")
    public ResponseEntity<MCPResponse<Tool>> getTool(@PathVariable String toolName) {
        try {
            return toolService.getToolByName(toolName)
                    .map(tool -> ResponseEntity.ok(MCPResponse.success(tool)))
                    .orElse(ResponseEntity.ok(MCPResponse.error("TOOL_NOT_FOUND", "Not Found: " + toolName)));
        } catch (Exception e) {
            log.error("Not Found: {}", toolName, e);
            return ResponseEntity.ok(MCPResponse.error("TOOL_ERROR", "Not Found: " + toolName));
        }
    }

    @PostMapping("/execute")
    public ResponseEntity<MCPResponse<Object>> executeTool(@RequestBody ToolExecutionRequest request) {
        try {
            return toolService.executeTool(request)
                    .map(result -> ResponseEntity.ok(MCPResponse.success(result)))
                    .orElse(ResponseEntity.ok(MCPResponse.error("EXECUTION_ERROR", "Not Found: " + request.getToolName())));
        } catch (Exception e) {
            log.error("Not Found: {}", request.getToolName(), e);
            return ResponseEntity.ok(MCPResponse.error("EXECUTION_ERROR", "Not Found: " + request.getToolName(), e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<MCPResponse<Map<String, String>>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        return ResponseEntity.ok(MCPResponse.success(status));
    }
} 