package com.CodeGraph.agent.service;

import com.CodeGraph.agent.dto.AgentConfigurationRequest;
import com.CodeGraph.agent.dto.AgentConfigurationResponse;
import com.CodeGraph.agent.dto.AgentConfigurationSearchRequest;
import com.CodeGraph.agent.mapper.AgentConfigurationMapper;
import com.CodeGraph.agent.model.AgentConfiguration;
import com.CodeGraph.common.response.BaseResponse;
import com.CodeGraph.common.response.PageMetaData;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AgentConfigurationService {

    private final AgentConfigurationMapper mapper;

    public AgentConfigurationService(
            AgentConfigurationMapper mapper) {

        this.mapper = mapper;
    }

    public BaseResponse<List<AgentConfigurationResponse>> getAll(
            Long userId,
            AgentConfigurationSearchRequest request) {

        int page = Math.max(request.getPage(), 1);

        int size = request.getSize();

        if (size < 1) {
            size = 5;
        }

        if (size > 100) {
            size = 100;
        }

        String search = request.getSearch();

        if (search != null && search.isBlank()) {
            search = null;
        }

        String sortBy = normalizeSortBy(
                request.getSortBy()
        );

        String sortDirection = normalizeSortDirection(
                request.getSortDirection()
        );

        int offset = (page - 1) * size;

        List<AgentConfigurationResponse> configurations =
                mapper.findAllByUserId(
                                userId,
                                search,
                                sortBy,
                                sortDirection,
                                offset,
                                size
                        )
                        .stream()
                        .map(AgentConfigurationResponse::from)
                        .toList();

        long totalElements =
                mapper.countByUserId(
                        userId,
                        search
                );

        int totalPages =
                (int) Math.ceil(
                        (double) totalElements / size
                );

        PageMetaData metaData =
                new PageMetaData(
                        page,
                        size,
                        totalElements,
                        totalPages
                );

        return new BaseResponse<>(
                configurations,
                metaData,
                "Agent configurations retrieved successfully",
                null,
                true,
                200
        );
    }

    private String normalizeSortBy(String sortBy) {

        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }

        return switch (sortBy) {
            case "agentId",
                 "name",
                 "host",
                 "port",
                 "protocol",
                 "createdAt",
                 "updatedAt" -> sortBy;

            default -> "createdAt";
        };
    }

    private String normalizeSortDirection(
            String sortDirection) {

        if ("asc".equalsIgnoreCase(sortDirection)) {
            return "asc";
        }

        return "desc";
    }

    public BaseResponse<AgentConfigurationResponse> get(
            Long userId,
            String agentId) {

        AgentConfiguration configuration =
                mapper.findByAgentIdAndUserId(
                        agentId,
                        userId
                );

        if (configuration == null) {
            return new BaseResponse<>(
                    null,
                    "Agent configuration not found",
                    null,
                    false,
                    404
            );
        }

        return new BaseResponse<>(
                AgentConfigurationResponse.from(configuration),
                "Agent configuration retrieved successfully",
                null,
                true,
                200
        );
    }

    public BaseResponse<AgentConfigurationResponse> create(
            Long userId,
            AgentConfigurationRequest request) {

        AgentConfiguration configuration =
                new AgentConfiguration();


        configuration.setAgentId(
                request.getAgentId()
        );
        configuration.setUserId(userId);
        configuration.setName(request.getName());
        configuration.setHost(request.getHost());
        configuration.setPort(request.getPort());
        configuration.setProtocol(
                request.getProtocol().toUpperCase()
        );

        mapper.insert(configuration);

        AgentConfiguration created =
                mapper.findByAgentIdAndUserId(
                        configuration.getAgentId(),
                        userId
                );

        return new BaseResponse<>(
                AgentConfigurationResponse.from(created),
                "Agent configuration created successfully",
                null,
                true,
                201
        );
    }

    public BaseResponse<AgentConfigurationResponse> update(
            Long userId,
            String agentId,
            AgentConfigurationRequest request) {

        AgentConfiguration existing =
                mapper.findByAgentIdAndUserId(
                        agentId,
                        userId
                );

        if (existing == null) {
            return new BaseResponse<>(
                    null,
                    "Agent configuration not found",
                    null,
                    false,
                    404
            );
        }

        int updated =
                mapper.update(
                        agentId,
                        userId,
                        request.getName(),
                        request.getHost(),
                        request.getPort(),
                        request.getProtocol().toUpperCase()
                );

        if (updated == 0) {
            return new BaseResponse<>(
                    null,
                    "Agent configuration update failed",
                    null,
                    false,
                    404
            );
        }

        AgentConfiguration updatedConfiguration =
                mapper.findByAgentIdAndUserId(
                        agentId,
                        userId
                );

        return new BaseResponse<>(
                AgentConfigurationResponse.from(
                        updatedConfiguration
                ),
                "Agent configuration updated successfully",
                null,
                true,
                200
        );
    }

    public BaseResponse<Void> delete(
            Long userId,
            String agentId) {

        int deleted =
                mapper.delete(
                        agentId,
                        userId
                );

        if (deleted == 0) {
            return new BaseResponse<>(
                    null,
                    "Agent configuration not found",
                    null,
                    false,
                    404
            );
        }

        return new BaseResponse<>(
                null,
                "Agent configuration deleted successfully",
                null,
                true,
                200
        );
    }
}