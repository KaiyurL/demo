package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;

public interface UserService {
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);

    // 获取用户分页数据
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);

    // 根据 id 获取用户信息
    Result<User> getUserById(Long id);

}
