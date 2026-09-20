package com.CodeGraph.agent.dto;

import com.CodeGraph.agent.model.AgentConfiguration;

import java.time.LocalDateTime;

public record AgentConfigurationResponse(
        String agentId,
        String name,
        String host,
        Integer port,
        String protocol,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AgentConfigurationResponse from(
            AgentConfiguration configuration) {

        return new AgentConfigurationResponse(
                configuration.getAgentId(),
                configuration.getName(),
                configuration.getHost(),
                configuration.getPort(),
                configuration.getProtocol(),
                configuration.getCreatedAt(),
                configuration.getUpdatedAt()
        );
    }
}