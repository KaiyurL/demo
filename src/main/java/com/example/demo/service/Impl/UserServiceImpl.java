package com.example.demo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service // 必须添加该注解，将业务类交给 Spring 容器管理
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<String> register(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User existed = userMapper.selectOne(queryWrapper);
        if (existed != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());

        int inserted = userMapper.insert(user);
        if (inserted != 1) {
            return Result.error(ResultCode.USER_CREATE_FAILED);
        }

        return Result.success("注册成功，用户ID=" + user.getId());
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        if (user.getPassword() == null || !user.getPassword().equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        String token = UUID.randomUUID().toString().replace("-", "");

        return Result.success(token, "登录成功");
    }

    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        // 1. 创建分页对象（参数 1：当前页码，参数 2：每页显示条数）
        Page<User> pageParam = new Page<>(pageNum, pageSize);

        // 2. 执行分页查询（参数 1：分页对象，参数 2：查询条件 Wrapper，这里传 null 代表查询全部）
        // 框架会自动执行一条 COUNT 语句查询总数，再拼接 LIMIT 执行分页
        Page<User> resultPage = userMapper.selectPage(pageParam, null);

        // 3. 返回结果（resultPage 中包含了 records 数据列表、total 总条数、pages 总页数）
        return Result.success(resultPage);
    }

    @Override
    public Result<User> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
