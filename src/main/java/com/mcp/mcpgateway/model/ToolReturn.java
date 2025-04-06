package com.mcp.mcpgateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolReturn {
    private String type;
    private String description;
    private Object schema;
} 