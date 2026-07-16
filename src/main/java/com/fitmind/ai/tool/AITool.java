package com.fitmind.ai.tool;

import java.util.Map;

public interface AITool {
    String getName();
    String getDescription();
    Map<String, String> getParameters();
    String execute(Long userId, Map<String, String> params);
}
