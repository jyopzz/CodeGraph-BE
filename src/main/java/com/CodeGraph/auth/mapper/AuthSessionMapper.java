package com.CodeGraph.auth.mapper;

import com.CodeGraph.auth.model.AuthSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface AuthSessionMapper {

    int insert(AuthSession authSession);

    AuthSession findBySessionId(
            @Param("sessionId") UUID sessionId
    );

    int updateAccessTokenId(
            @Param("sessionId") UUID sessionId,
            @Param("accessTokenId") UUID accessTokenId
    );

    int revoke(
            @Param("sessionId") UUID sessionId
    );

    int revokeAllByUserId(
            @Param("userId") Long userId
    );
}