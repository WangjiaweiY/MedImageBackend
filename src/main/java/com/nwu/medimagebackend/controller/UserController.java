package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.DTO.LoginDTO;
import com.nwu.medimagebackend.entity.Result;
import com.nwu.medimagebackend.entity.User;
import com.nwu.medimagebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * <p>
 * 处理用户相关的HTTP请求，包括用户登录和注册等操作。
 * 该控制器负责验证用户输入，调用相应的服务层方法，并构建适当的响应。
 * </p>
 * 
 * @author MedImage团队
 */
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * <p>
     * 验证用户凭据并返回用户信息或错误提示。
     * </p>
     * 
     * @param user 包含用户名和密码的用户对象
     * @return 登录成功返回用户信息，失败返回错误信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            log.info("用户[{}]尝试登录", user.getUsername());
            User loginUser = userService.login(user);
            if (loginUser != null) {
                log.info("用户[{}]登录成功", user.getUsername());
                return ResponseEntity.ok(loginUser);
            } else {
                log.warn("用户[{}]登录失败：凭据无效", user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("用户[{}]登录过程中发生异常: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 用户注册
     * <p>
     * 注册新用户并返回结果。
     * </p>
     * 
     * @param user 包含用户信息的用户对象
     * @return 注册成功返回成功消息，失败返回错误信息
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            log.info("尝试注册新用户[{}]", user.getUsername());
            userService.register(user);
            log.info("用户[{}]注册成功", user.getUsername());
            return ResponseEntity.ok("注册成功");
        } catch (Exception e) {
            log.error("用户[{}]注册失败: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
