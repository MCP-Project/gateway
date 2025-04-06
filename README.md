# MCP Gateway

Model Context Protocol (MCP) Gateway - A tool management and execution service.

## Overview

MCP Gateway provides a central service for registering, discovering, and executing various tools. It acts as a gateway between applications and specialized tools, offering a standardized interface for tool execution.

## Features

- Tool discovery and registration
- Standardized tool execution interface
- Support for multiple tool types:
  - Calculator for mathematical operations
  - Text analyzer for text processing
  - Weather forecast for retrieving weather information

## Architecture

The MCP Gateway follows a modular architecture:

1. **Tools Registry**: Central repository of all available tools
2. **Tool Handlers**: Implementations for specific tool functionalities
3. **Protocol Layer**: REST API for tool discovery and execution

## How to Use

### Running the Gateway

```bash
mvn clean package
java -jar target/mcp-gateway-0.0.1-SNAPSHOT.jar
```

The gateway will start on port 8080 by default.

### Available Endpoints

#### Tool Management
- GET `/mcp/api/tools` - List all available tools
- GET `/mcp/api/tools/{toolName}` - Get details about a specific tool
- POST `/mcp/api/tools/{toolName}/execute` - Execute a specific tool

#### Health Check
- GET `/mcp/api/health` - Check gateway health

### Example Requests

##### List Tools
```bash
curl -X GET http://localhost:8080/mcp/api/tools
```

##### Execute Calculator
```bash
curl -X POST http://localhost:8080/mcp/api/tools/calculator/execute \
  -H "Content-Type: application/json" \
  -d '{"operation": "add", "a": 10, "b": 5}'
```

## Configuration

Configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/mcp/api

# Logging Configuration
logging.level.com.mcp.mcpgateway=DEBUG
```

## Extending with New Tools

To add a new tool to the gateway:

1. Create a new implementation of the `ToolHandler` interface
2. Register the tool handler as a Spring component
3. The tool will be automatically discovered and made available via the API 