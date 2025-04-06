package com.mcp.mcpgateway.service;

import java.util.List;
import java.util.Optional;

import com.mcp.mcpgateway.model.Tool;
import com.mcp.mcpgateway.model.ToolExecutionRequest;


public interface ToolService {
    
    List<Tool> getAllTools();    

    Optional<Tool> getToolByName(String name);
    
    Optional<Object> executeTool(ToolExecutionRequest request);
} 