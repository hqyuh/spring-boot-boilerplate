package com.hqh.boilerplate.mybatis;

import com.hqh.boilerplate.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserQueryMapper {

    List<User> search(@Param("keyword") String keyword,
                      @Param("role") String role,
                      @Param("active") Boolean active);
}
