package com.fitmind.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssistantRequest {
    @NotBlank(message = "Query cannot be blank")
    private String query;
}
