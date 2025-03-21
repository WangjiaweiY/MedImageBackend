package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.DTO.LoginDTO;
import com.nwu.medimagebackend.entity.User;

public interface UserService {
    /**
     * 注册新用户
     * @param user 用户信息
     * @throws Exception 当用户名已存在或其他业务逻辑错误时抛出异常
     */
    void register(User user) throws Exception;

    /**
     * 用户登录
     * @param user 登录时传入的用户名和密码
     * @return 如果登录成功，返回完整用户信息，否则返回 null
     * @throws Exception 当出现业务异常时抛出
     */
    User login(User user) throws Exception;
}
