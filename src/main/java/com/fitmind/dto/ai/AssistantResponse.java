package com.fitmind.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AssistantResponse {
    private String query;
    private String response;
    private String intent;
    private List<String> dataSources;
    private LocalDateTime timestamp;
}
