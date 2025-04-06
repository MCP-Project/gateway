package com.mcp.mcpgateway.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    private String name;
    private String description;
    private List<ToolParameter> parameters;
    private ToolReturn returns;
    private Map<String, Object> metadata;
} 