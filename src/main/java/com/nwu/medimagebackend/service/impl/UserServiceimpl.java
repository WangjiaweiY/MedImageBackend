package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.DTO.LoginDTO;
import com.nwu.medimagebackend.entity.User;
import com.nwu.medimagebackend.mapper.UserMapper;
import com.nwu.medimagebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class UserServiceimpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public User login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        User user = userMapper.getByUsername(username);

        if (user == null) {
            // 用户不存在，返回 null 或者抛出自定义异常
            throw new RuntimeException("用户名不存在");
        }
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!user.getPassword().equals(password)) {
            // 密码不匹配，返回 null 或者抛出自定义异常
            throw new RuntimeException("密码错误");
        }

        return user;
    }
}
