package com.forge.ai.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<AITool> tools;
    private final Map<String, AITool> toolMap = new LinkedHashMap<>();

    @PostConstruct
    void init() {
        for (AITool t : tools) toolMap.put(t.getName(), t);
    }

    public AITool getTool(String name) { return toolMap.get(name); }
    public Map<String, AITool> getAllTools() { return toolMap; }
    public String describeAll() {
        StringBuilder sb = new StringBuilder("Available tools:\n");
        for (AITool t : tools) {
            sb.append("- ").append(t.getName()).append(": ").append(t.getDescription()).append("\n");
        }
        return sb.toString();
    }
}
