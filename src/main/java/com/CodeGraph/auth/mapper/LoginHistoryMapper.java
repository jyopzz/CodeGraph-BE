package com.CodeGraph.auth.mapper;

import com.CodeGraph.auth.model.LoginHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginHistoryMapper {

    int insert(LoginHistory loginHistory);
}