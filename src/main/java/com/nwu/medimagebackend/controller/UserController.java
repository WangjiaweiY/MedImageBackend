package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.DTO.LoginDTO;
import com.nwu.medimagebackend.common.Result;
import com.nwu.medimagebackend.entity.User;
import com.nwu.medimagebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginDTO loginDTO) {
        log.info("登录信息:{}", loginDTO);

        User user = userService.login(loginDTO);

        return Result.success(user);
    }

}
