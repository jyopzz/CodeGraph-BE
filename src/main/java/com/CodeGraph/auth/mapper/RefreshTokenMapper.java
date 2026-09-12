package com.CodeGraph.auth.mapper;

import com.CodeGraph.auth.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken refreshToken);

    RefreshToken findByTokenId(
            @Param("tokenId") String tokenId
    );

    int revoke(
            @Param("id") Long id
    );

    int revokeAllByUserId(
            @Param("userId") Long userId
    );

    int revokeBySessionId(
            @Param("sessionId") UUID sessionId
    );
}