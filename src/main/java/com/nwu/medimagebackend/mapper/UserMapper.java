package com.nwu.medimagebackend.mapper;

import com.nwu.medimagebackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from users where username = #{username}")
    public User getByUsername(String username);
}
