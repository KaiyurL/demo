package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserInfo;
import com.example.demo.vo.UserDetailVO;

public interface UserService {
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);

    // 获取用户分页数据
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);

    // 根据 id 获取用户信息
    Result<User> getUserById(Long id);

    // 查询用户详情（多表联查 + Redis）
    Result<UserDetailVO> getUserDetail(Long userId);

    // 更新用户扩展信息
    Result<String> updateUserInfo(UserInfo userInfo);

    // 删除用户
    Result<String> deleteUser(Long userId);
}
