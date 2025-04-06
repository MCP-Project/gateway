package com.mcp.mcpgateway.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcp.mcpgateway.config.RemoteServicesConfig;
import com.mcp.mcpgateway.model.Tool;
import com.mcp.mcpgateway.service.RemoteServiceManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/admin/remote-services")
@RequiredArgsConstructor
public class RemoteServiceController {

    private final RemoteServicesConfig config;
    private final RemoteServiceManager remoteServiceManager;

    @GetMapping("/config")
    public ResponseEntity<RemoteServicesConfig> getConfiguration() {
        return ResponseEntity.ok(config);
    }

    @GetMapping("/tools")
    public ResponseEntity<List<Tool>> getRemoteTools() {
        List<Tool> tools = remoteServiceManager.getAllRemoteTools();
        return ResponseEntity.ok(tools);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshTools() {
        log.info("Manual refresh of remote tools requested");
        remoteServiceManager.refreshRemoteTools();
        
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Remote tools refreshed successfully",
                "toolCount", remoteServiceManager.getAllRemoteTools().size()
        ));
    }
} 