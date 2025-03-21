package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.DTO.LoginDTO;
import com.nwu.medimagebackend.entity.User;
import com.nwu.medimagebackend.mapper.UserMapper;
import com.nwu.medimagebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class UserServiceimpl implements UserService {

    @Autowired
    UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(User user) throws Exception {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new Exception("用户名已存在");
        }
        // 使用 BCrypt 进行密码加密
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        userMapper.insertUser(user);
    }

    @Override
    public User login(User user) throws Exception {
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser == null) {
            throw new Exception("用户不存在");
        }
        // 使用 BCrypt 进行密码匹配
        if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            throw new Exception("密码错误");
        }
        return existingUser;
    }
}
