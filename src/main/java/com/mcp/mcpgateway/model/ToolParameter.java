package com.mcp.mcpgateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolParameter {
    private String name;
    private String type;
    private String description;
    private boolean required;
    private Object schema;
} 