package com.CodeGraph.agent.mapper;

import com.CodeGraph.agent.model.AgentConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentConfigurationMapper {

    List<AgentConfiguration> findAllByUserId(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection,
            @Param("offset") int offset,
            @Param("size") int size);

    long countByUserId(
            @Param("userId") Long userId,
            @Param("search") String search);

    AgentConfiguration findByAgentIdAndUserId(
            @Param("agentId") String agentId,
            @Param("userId") Long userId
    );

    int insert(AgentConfiguration configuration);

    int update(
            @Param("agentId") String agentId,
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("host") String host,
            @Param("port") Integer port,
            @Param("protocol") String protocol
    );

    int delete(
            @Param("agentId") String agentId,
            @Param("userId") Long userId
    );
}