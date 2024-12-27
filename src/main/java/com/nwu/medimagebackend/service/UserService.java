package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.DTO.LoginDTO;
import com.nwu.medimagebackend.entity.User;

public interface UserService {
    User login(LoginDTO loginDTO);
}
