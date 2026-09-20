package com.CodeGraph.agent.controller;

import com.CodeGraph.agent.dto.AgentConfigurationRequest;
import com.CodeGraph.agent.dto.AgentConfigurationResponse;
import com.CodeGraph.agent.dto.AgentConfigurationSearchRequest;
import com.CodeGraph.agent.service.AgentConfigurationService;
import com.CodeGraph.auth.security.AuthenticatedUser;
import com.CodeGraph.common.response.BaseResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-configurations")
public class AgentConfigurationController {

    private final AgentConfigurationService service;

    public AgentConfigurationController(
            AgentConfigurationService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<
            BaseResponse<
                    java.util.List<AgentConfigurationResponse>
                    >
            > getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        AgentConfigurationSearchRequest request =
                new AgentConfigurationSearchRequest();

        request.setSearch(search);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        var response =
                service.getAll(
                        user.userId(),
                        request
                );

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<
            BaseResponse<AgentConfigurationResponse>
            > get(
            @PathVariable String agentId,
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        var response =
                service.get(
                        user.userId(),
                        agentId
                );

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @PostMapping
    public ResponseEntity<
            BaseResponse<AgentConfigurationResponse>
            > create(
            @Valid @RequestBody AgentConfigurationRequest request,
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        var response =
                service.create(
                        user.userId(),
                        request
                );

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @PatchMapping("/{agentId}")
    public ResponseEntity<
            BaseResponse<AgentConfigurationResponse>
            > update(
            @PathVariable String agentId,
            @Valid @RequestBody AgentConfigurationRequest request,
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        var response =
                service.update(
                        user.userId(),
                        agentId,
                        request
                );

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @DeleteMapping("/{agentId}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable String agentId,
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        var response =
                service.delete(
                        user.userId(),
                        agentId
                );

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {

        System.out.println(
                "########## AGENT CONFIG PING HIT ##########"
        );

        return ResponseEntity.ok("agent-configurations OK");
    }
}