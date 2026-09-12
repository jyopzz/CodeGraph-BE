package com.CodeGraph.auth.mapper;

import com.CodeGraph.auth.model.EmailVerification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailVerificationMapper {

    int save(EmailVerification emailVerification);

    int invalidateActiveOtps(
            String email,
            String purpose
    );

    EmailVerification findLatest(
            String email,
            String purpose
    );

    int incrementAttempts(Long id);

    int markVerified(Long id);
}