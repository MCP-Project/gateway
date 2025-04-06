package com.mcp.mcpgateway.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mcp.mcpgateway.model.Tool;
import com.mcp.mcpgateway.model.ToolExecutionRequest;
import com.mcp.mcpgateway.model.ToolParameter;
import com.mcp.mcpgateway.model.ToolReturn;
import com.mcp.mcpgateway.service.ToolService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class MockToolServiceImpl implements ToolService {

    private final Map<String, Tool> tools = new HashMap<>();
    
    
    @PostConstruct
    public void init() {
        createMockTools();
    }
    
    @Override
    public List<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }

    @Override
    public Optional<Tool> getToolByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    @Override
    public Optional<Object> executeTool(ToolExecutionRequest request) {
        String toolName = request.getToolName();
        Map<String, Object> params = request.getParameters();
        
        log.info("Running Tool: {} with parameters: {}", toolName, params);
        
        
        Tool tool = tools.get(toolName);
        if (tool == null) {
            log.error("Not Found: {}", toolName);
            return Optional.empty();
        }

        switch (toolName) {
            case "calculator":
                return executeCalculator(params);
            case "text-analyzer":
                return executeTextAnalyzer(params);
            case "weather-forecast":
                return executeWeatherForecast(params);
            default:
                log.error("Not Found: {}", toolName);
                return Optional.empty();
        }
    }
    
    private Optional<Object> executeCalculator(Map<String, Object> params) {
        try {
            String operation = (String) params.get("operation");
            Double a = Double.parseDouble(params.get("a").toString());
            Double b = Double.parseDouble(params.get("b").toString());
            
            Double result = null;
            switch (operation) {
                case "add":
                    result = a + b;
                    break;
                case "subtract":
                    result = a - b;
                    break;
                case "multiply":
                    result = a * b;
                    break;
                case "divide":
                    if (b == 0) {
                        throw new ArithmeticException("Divided by zero");
                    }
                    result = a / b;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid: " + operation);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("result", result);
            response.put("operation", operation);
            
            return Optional.of(response);
        } catch (Exception e) {
            log.error("Error", e);
            return Optional.empty();
        }
    }
    
    private Optional<Object> executeTextAnalyzer(Map<String, Object> params) {
        try {
            String text = (String) params.get("text");
            
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("length", text.length());
            analysis.put("wordCount", text.split("\\s+").length);
            analysis.put("uppercase", text.toUpperCase());
            
            return Optional.of(analysis);
        } catch (Exception e) {
            log.error("Error", e);
            return Optional.empty();
        }
    }
    
    private Optional<Object> executeWeatherForecast(Map<String, Object> params) {
        try {
            String city = (String) params.get("city");
            
            Map<String, Object> forecast = new HashMap<>();
            forecast.put("city", city);
            forecast.put("temperature", 25.5);
            forecast.put("condition", "Sunny");
            forecast.put("humidity", 70);
            
            return Optional.of(forecast);
        } catch (Exception e) {
            log.error("Error", e);
            return Optional.empty();
        }
    }
        
    private void createMockTools() {
        Tool calculator = Tool.builder()
                .name("calculator")
                .description("Performs basic mathematical operations")
                .parameters(Arrays.asList(
                        ToolParameter.builder()
                                .name("operation")
                                .type("string")
                                .description("Operation to perform (add, subtract, multiply, divide)")
                                .required(true)
                                .build(),
                        ToolParameter.builder()
                                .name("a")
                                .type("number")
                                .description("First operand")
                                .required(true)
                                .build(),
                        ToolParameter.builder()
                                .name("b")
                                .type("number")
                                .description("Second operand")
                                .required(true)
                                .build()
                ))
                .returns(ToolReturn.builder()
                        .type("object")
                        .description("Operation result")
                        .build())
                .build();
        
        Tool textAnalyzer = Tool.builder()
                .name("text-analyzer")
                .description("Analyzes text and returns information about it")
                .parameters(Arrays.asList(
                        ToolParameter.builder()
                                .name("text")
                                .type("string")
                                .description("Text to analyze")
                                .required(true)
                                .build()
                ))
                .returns(ToolReturn.builder()
                        .type("object")
                        .description("Text analysis")
                        .build())
                .build();
        
        Tool weatherForecast = Tool.builder()
                .name("weather-forecast")
                .description("Returns weather forecast for a city")
                .parameters(Arrays.asList(
                        ToolParameter.builder()
                                .name("city")
                                .type("string")
                                .description("City name")
                                .required(true)
                                .build()
                ))
                .returns(ToolReturn.builder()
                        .type("object")
                        .description("Weather forecast")
                        .build())
                .build();
        
        
        
        tools.put(calculator.getName(), calculator);
        tools.put(textAnalyzer.getName(), textAnalyzer);
        tools.put(weatherForecast.getName(), weatherForecast);
        
    }
    
} 