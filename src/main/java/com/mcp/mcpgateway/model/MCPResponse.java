package com.mcp.mcpgateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPResponse<T> {
    private boolean success;
    private T data;
    private ErrorDetails error;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private Object details;
    }

    public static <T> MCPResponse<T> success(T data) {
        return MCPResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> MCPResponse<T> error(String code, String message, Object details) {
        ErrorDetails error = ErrorDetails.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
        
        return MCPResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    public static <T> MCPResponse<T> error(String code, String message) {
        return error(code, message, null);
    }
} 