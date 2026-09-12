package com.CodeGraph.profile.mapper;

import com.CodeGraph.profile.model.Profile;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface ProfileMapper {

    Profile findByUserId(Long userId);

    void save(Profile profile);

    int update(
            Long userId,
            Map<String, Object> changedFields);
}
